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
 * Simulates a simple process instance observing ten messages and completing after the tenth message, creating a task per message, without relations
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SimpleProcessScenario implements TestScenario {

    public static final String PROCESS_COUNT = "processCount";
    public static final String WARM_UP_PROCESS_COUNT = "warmUpProcessCount";
    public static final String DURATION_MINUTES = "durationMinutes";

    static final int TEN_MESSAGES = 10;

    private final AsyncKafkaMessagePublisher messagePublisher;
    private final LoadGenerator loadGenerator;

    private int processCount;
    private int warmUpProcessCount;
    private int durationMinutes;

    @Override
    public TestScenarioType getScenarioType() {
        return TestScenarioType.SIMPLE_PROCESS;
    }

    @Override
    public void prepare(TestRun testRun) {
        processCount = testRun.getParameterOrDefault(PROCESS_COUNT, 100);
        warmUpProcessCount = testRun.getParameterOrDefault(WARM_UP_PROCESS_COUNT, 10);
        durationMinutes = testRun.getParameterOrDefault(DURATION_MINUTES, 0);
    }

    @Override
    public void warmUp(TestRun testRun) {
        loadGenerator.generateBurst(warmUpProcessCount, _ -> {
            String originProcessId = createProcessInstance(testRun);
            produceMessages(originProcessId, testRun);
        });
        testRun.waitUntilAllProcessesCompleted();
    }

    @Override
    public void run(TestRun testRun) {
        loadGenerator.generateDistributedOverTime(processCount, Duration.ofMinutes(durationMinutes), _ -> {
            String originProcessId = createProcessInstance(testRun);
            produceMessages(originProcessId, testRun);
        });
        testRun.waitUntilAllProcessesCompleted();
    }

    private String createProcessInstance(TestRun testRun) {
        String originProcessId = getScenarioType() + "-" + UUID.randomUUID();
        var event = messagePublisher.raceValidated(originProcessId);
        testRun.incrementMessageCount();
        testRun.recordCreatedProcess(new TestProcessInstance(originProcessId, LocalDateTime.ofInstant(event.getIdentity().getCreated(), ZoneId.systemDefault())));
        return originProcessId;
    }

    private void produceMessages(String originProcessId, TestRun testRun) {
        for (int i = 0; i < TEN_MESSAGES; i++) {
            messagePublisher.raceControlpointPassed(originProcessId, "perftest-" + i);
            testRun.incrementMessageCount();
        }
    }
}
