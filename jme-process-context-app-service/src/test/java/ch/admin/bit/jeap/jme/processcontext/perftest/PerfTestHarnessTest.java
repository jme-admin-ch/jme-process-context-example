package ch.admin.bit.jeap.jme.processcontext.perftest;

import ch.admin.bit.jeap.jme.processcontext.perftest.scenario.TestScenario;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PerfTestHarnessTest {

    @Test
    void execute_successfulRun_completesAllPhases() {
        List<String> phases = new ArrayList<>();
        TestScenario scenario = stubScenario(phases);
        PerfTestHarness harness = new PerfTestHarness(List.of(scenario));
        TestRun testRun = testRun();

        harness.execute(testRun);

        assertEquals(List.of("prepare", "warmUp", "run", "verify"), phases);
        assertEquals(TestRunStatus.COMPLETED, testRun.getStatus());
        assertNotNull(testRun.getTestReport());
        assertNotNull(testRun.getWarmUpStartedAt());
        assertNotNull(testRun.getStartedAt());
        assertNotNull(testRun.getFinishedAt());
    }

    @Test
    void execute_scenarioRunFails_setsStatusToFailed() {
        TestScenario scenario = failingScenario("run", "something broke");
        PerfTestHarness harness = new PerfTestHarness(List.of(scenario));
        TestRun testRun = testRun();

        harness.execute(testRun);

        assertEquals(TestRunStatus.FAILED, testRun.getStatus());
        assertEquals("something broke", testRun.getErrorMessage());
        assertNotNull(testRun.getTestReport());
    }

    @Test
    void execute_scenarioVerifyFails_setsStatusToFailed() {
        TestScenario scenario = failingScenario("verify", "assertion failed");
        PerfTestHarness harness = new PerfTestHarness(List.of(scenario));
        TestRun testRun = testRun();

        harness.execute(testRun);

        assertEquals(TestRunStatus.FAILED, testRun.getStatus());
        assertEquals("assertion failed", testRun.getErrorMessage());
        assertNotNull(testRun.getTestReport());
    }

    @Test
    void execute_unknownScenarioType_throwsIllegalArgument() {
        PerfTestHarness harness = new PerfTestHarness(List.of());
        TestRun testRun = testRun();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> harness.execute(testRun));

        assertTrue(ex.getMessage().contains("No implementation found for scenario"));
    }

    @Test
    void execute_resetsAfterWarmUp() {
        List<String> phases = new ArrayList<>();
        TestScenario scenario = new TestScenario() {
            @Override
            public TestScenarioType getScenarioType() {
                return TestScenarioType.HIGH_MESSAGE_COUNT;
            }

            @Override
            public void prepare(TestRun testRun) {
                // No-op for test stub
            }

            @Override
            public void warmUp(TestRun testRun) {
                testRun.incrementMessageCount();
                testRun.incrementMessageCount();
            }

            @Override
            public void run(TestRun testRun) {
                phases.add("messageCount=" + testRun.getMessageCount());
                phases.add("processCount=" + testRun.getProcessInstances().size());
            }

            @Override
            public void verify(TestRun testRun) {
                // No-op for test stub
            }
        };
        PerfTestHarness harness = new PerfTestHarness(List.of(scenario));
        TestRun testRun = testRun();

        harness.execute(testRun);

        assertEquals(List.of("messageCount=0", "processCount=0"), phases);
    }

    private static TestRun testRun() {
        TestProcessInstanceRepository repository = new TestProcessInstanceRepository(null);
        return new TestRun(repository, UUID.randomUUID(), TestScenarioType.HIGH_MESSAGE_COUNT, Map.of(), Duration.ZERO, false);
    }

    private static TestScenario stubScenario(List<String> phases) {
        return new TestScenario() {
            @Override
            public TestScenarioType getScenarioType() {
                return TestScenarioType.HIGH_MESSAGE_COUNT;
            }

            @Override
            public void prepare(TestRun testRun) {
                phases.add("prepare");
            }

            @Override
            public void warmUp(TestRun testRun) {
                phases.add("warmUp");
            }

            @Override
            public void run(TestRun testRun) {
                phases.add("run");
            }

            @Override
            public void verify(TestRun testRun) {
                phases.add("verify");
            }
        };
    }

    private static TestScenario failingScenario(String failInPhase, String message) {
        return new TestScenario() {
            @Override
            public TestScenarioType getScenarioType() {
                return TestScenarioType.HIGH_MESSAGE_COUNT;
            }

            @Override
            public void prepare(TestRun testRun) {
                if ("prepare".equals(failInPhase)) {
                    throw new RuntimeException(message);
                }
            }

            @Override
            public void warmUp(TestRun testRun) {
                if ("warmUp".equals(failInPhase)) {
                    throw new RuntimeException(message);
                }
            }

            @Override
            public void run(TestRun testRun) {
                if ("run".equals(failInPhase)) {
                    throw new RuntimeException(message);
                }
            }

            @Override
            public void verify(TestRun testRun) {
                if ("verify".equals(failInPhase)) {
                    throw new RuntimeException(message);
                }
            }
        };
    }
}
