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
import java.util.UUID;

/**
 * Simulates process instances that exercise ProcessContext query methods in the completion condition.
 * Each process is created with a JmeDocumentReviewedEvent and completes after the configured number of events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessContextQueriesScenario implements TestScenario {

    public static final String PROCESS_COUNT = "processCount";
    public static final String WARM_UP_PROCESS_COUNT = "warmUpProcessCount";
    public static final String DURATION_MINUTES = "durationMinutes";
    public static final String MESSAGE_PER_PROCESS_COUNT = "messagePerProcessCount";

    private final AsyncKafkaMessagePublisher messagePublisher;
    private final LoadGenerator loadGenerator;

    private int processCount;
    private int warmUpProcessCount;
    private int durationMinutes;
    private int messagePerProcessCount;

    @Override
    public TestScenarioType getScenarioType() {
        return TestScenarioType.PROCESS_CONTEXT_QUERIES;
    }

    @Override
    public void prepare(TestRun testRun) {
        processCount = testRun.getParameterOrDefault(PROCESS_COUNT, 100);
        warmUpProcessCount = testRun.getParameterOrDefault(WARM_UP_PROCESS_COUNT, 10);
        durationMinutes = testRun.getParameterOrDefault(DURATION_MINUTES, 0);
        messagePerProcessCount = testRun.getParameterOrDefault(MESSAGE_PER_PROCESS_COUNT, 10);
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
        messagePublisher.documentReviewCreated(originProcessId, UUID.randomUUID().toString(), "pending");
        testRun.incrementMessageCount();
        testRun.recordCreatedProcess(new TestProcessInstance(originProcessId, LocalDateTime.now()));
        return originProcessId;
    }

    private void produceMessages(String originProcessId, TestRun testRun) {
        for (int i = 1; i < messagePerProcessCount; i++) {
            messagePublisher.documentReviewCreated(originProcessId, UUID.randomUUID().toString(), "reviewed-" + i);
            testRun.incrementMessageCount();
        }
    }
}
