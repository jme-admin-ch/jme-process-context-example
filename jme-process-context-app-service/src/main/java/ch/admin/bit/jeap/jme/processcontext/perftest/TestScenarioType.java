package ch.admin.bit.jeap.jme.processcontext.perftest;

import lombok.Getter;

/**
 * Available load/performance test scenarios.
 */
@Getter
public enum TestScenarioType {

    HIGH_MESSAGE_COUNT("High Message Count", "Simulates process instances with a large number of correlated messages and created relations"),
    SIMPLE_PROCESS("Simple Process", "Simulates a simple process instance observing ten messages and completing after the tenth message, creating a task per message, without relations"),
    PROCESS_RELATIONS("Process Relations", "Simulates a process instance with a large number of process relations to other process instances");

    private final String label;
    private final String description;

    TestScenarioType(String label, String description) {
        this.label = label;
        this.description = description;
    }
}
