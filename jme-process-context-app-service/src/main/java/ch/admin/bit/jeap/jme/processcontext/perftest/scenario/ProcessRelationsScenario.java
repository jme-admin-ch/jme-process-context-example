package ch.admin.bit.jeap.jme.processcontext.perftest.scenario;

import ch.admin.bit.jeap.jme.processcontext.kafka.AsyncKafkaMessagePublisher;
import ch.admin.bit.jeap.jme.processcontext.perftest.TestProcessInstance;
import ch.admin.bit.jeap.jme.processcontext.perftest.TestRun;
import ch.admin.bit.jeap.jme.processcontext.perftest.TestScenarioType;
import ch.admin.bit.jeap.jme.processcontext.perftest.load.LoadGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Simulates process instances with a large number of process relations
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessRelationsScenario implements TestScenario {

    public static final String PROCESS_COUNT = "processCount";
    public static final String WARM_UP_PROCESS_COUNT = "warmUpProcessCount";
    public static final String DURATION_MINUTES = "durationMinutes";

    private final AsyncKafkaMessagePublisher messagePublisher;
    private final LoadGenerator loadGenerator;

    private int processCount;
    private int warmUpProcessCount;
    private int durationMinutes;

    @Override
    public TestScenarioType getScenarioType() {
        return TestScenarioType.PROCESS_RELATIONS;
    }

    @Override
    public void prepare(TestRun testRun) {
        processCount = testRun.getParameterOrDefault(PROCESS_COUNT, 100);
        warmUpProcessCount = testRun.getParameterOrDefault(WARM_UP_PROCESS_COUNT, 10);
        durationMinutes = testRun.getParameterOrDefault(DURATION_MINUTES, 0);
    }

    @Override
    public void warmUp(TestRun testRun) {
        String originProcessIdPrefix = getScenarioType() + "-" + UUID.randomUUID() + "-";
        loadGenerator.generateBurst(warmUpProcessCount, counter -> {
            createProcessInstance(testRun, counter, originProcessIdPrefix);
            produceMessages(originProcessIdPrefix, counter, testRun);
        });
        testRun.waitUntilAllProcessesCompleted();
    }

    @Override
    public void run(TestRun testRun) {
        String originProcessIdPrefix = getScenarioType() + "-" + UUID.randomUUID() + "-";
        loadGenerator.generateDistributedOverTime(processCount, Duration.ofMinutes(durationMinutes), counter -> {
            createProcessInstance(testRun, counter, originProcessIdPrefix);
            produceMessages(originProcessIdPrefix, counter, testRun);
        });
        testRun.waitUntilAllProcessesCompleted();
    }

    private void createProcessInstance(TestRun testRun, int counter, String originProcessIdPrefix) {
        String originProcessId = originProcessIdPrefix + counter;
        var event = messagePublisher.carRefuellingCompleted(originProcessId);
        testRun.incrementMessageCount();
        testRun.recordCreatedProcess(new TestProcessInstance(originProcessId, LocalDateTime.ofInstant(event.getIdentity().getCreated(), ZoneId.systemDefault())));
    }

    private void produceMessages(String originProcessIdPrefix, int counter, TestRun testRun) {
        String originProcessId = originProcessIdPrefix + counter;
        // Each message will create a relation to the first 1000 process instances.
        // See JmeCancelRaceCommandPerfTestReferenceExtractor
        messagePublisher.cancelRace(originProcessId, originProcessIdPrefix);
        testRun.incrementMessageCount();
    }
}
