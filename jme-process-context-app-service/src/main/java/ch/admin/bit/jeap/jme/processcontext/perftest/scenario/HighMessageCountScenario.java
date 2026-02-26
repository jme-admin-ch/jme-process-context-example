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
 * Simulates process instances with a large number of correlated messages.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HighMessageCountScenario implements TestScenario {

    public static final String MESSAGE_PER_PROCESS_COUNT = "messagePerProcessCount";
    public static final String TASKS_PER_PROCESS_COUNT = "tasksPerProcessCount";
    public static final String PROCESS_COUNT = "processCount";
    public static final String WARM_UP_PROCESS_COUNT = "warmUpProcessCount";
    public static final String DURATION_MINUTES = "durationMinutes";

    private final AsyncKafkaMessagePublisher messagePublisher;
    private final LoadGenerator loadGenerator;

    private int tasksPerProcessCount;
    private int messagePerProcessCount;
    private int processCount;
    private int warmUpProcessCount;
    private int durationMinutes;

    @Override
    public TestScenarioType getScenarioType() {
        return TestScenarioType.HIGH_MESSAGE_COUNT;
    }

    @Override
    public void prepare(TestRun testRun) {
        tasksPerProcessCount = testRun.getParameterOrDefault(TASKS_PER_PROCESS_COUNT, 100);
        messagePerProcessCount = testRun.getParameterOrDefault(MESSAGE_PER_PROCESS_COUNT, 100);
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

    @Override
    public void verify(TestRun testRun) {
        // Processes complete in this scenario when all expected messages have been received, so we can rely on the
        // process completion as a verification of message processing.

        // The perfestHighMessageCount template creates a relation for each control point passed message
        testRun.assertRelationCount(messagePerProcessCount);
    }

    private String createProcessInstance(TestRun testRun) {
        String id = UUID.randomUUID().toString();
        String originProcessId = getScenarioType() + "-" + id;
        String raceCarId = "raceCar-" + id;
        String controlPointMessageCount = String.valueOf(messagePerProcessCount);
        var event = messagePublisher.raceStarted(originProcessId, id, raceCarId, controlPointMessageCount);
        testRun.incrementMessageCount();
        testRun.recordCreatedProcess(new TestProcessInstance(originProcessId, LocalDateTime.ofInstant(event.getIdentity().getCreated(), ZoneId.systemDefault())));
        return originProcessId;
    }

    private void produceMessages(String originProcessId, TestRun testRun) {
        for (int i = 0; i < tasksPerProcessCount; i++) {
            messagePublisher.objectsOnTheRoadSpotted(originProcessId, false);
            testRun.incrementMessageCount();
        }
        for (int i = 0; i < messagePerProcessCount; i++) {
            messagePublisher.raceControlpointPassed(originProcessId, "perftest-" + i);
            testRun.incrementMessageCount();
        }
    }
}
