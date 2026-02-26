package ch.admin.bit.jeap.jme.processcontext.perftest.scenario;

import ch.admin.bit.jeap.jme.processcontext.perftest.TestRun;
import ch.admin.bit.jeap.jme.processcontext.perftest.TestScenarioType;

/**
 * Defines the lifecycle phases of a performance test scenario.
 * Each scenario implementation provides its own logic for preparation, warm-up,
 * execution, assertion, and teardown.
 */
public interface TestScenario {

    /**
     * Returns the scenario type this implementation handles.
     */
    TestScenarioType getScenarioType();

    /**
     * Clears the database and ensures a clean state for the test run.
     */
    void prepare(TestRun testRun);

    /**
     * Warms up the Process Context Service by creating a small number of test process instances.
     */
    void warmUp(TestRun testRun);

    /**
     * Executes the test scenario.
     */
    void run(TestRun testRun);

    /**
     * Asserts expected outcomes and collects performance metrics.
     */
    default void verify(TestRun testRun) {
    }
}
