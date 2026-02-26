package ch.admin.bit.jeap.jme.processcontext.perftest;

import ch.admin.bit.jeap.jme.processcontext.perftest.scenario.TestScenario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Test harness that orchestrates performance test execution through a defined lifecycle:
 * <ol>
 *   <li><b>Prepare</b> - Clear the database and run scenario-specific preparation.</li>
 *   <li><b>Warm up</b> - Create a few process instances to warm up the PCS, then reset tracking state.</li>
 *   <li><b>Run</b> - Execute the selected test scenario's load generation.</li>
 *   <li><b>Collect stats</b> - Query process instance state from the database and compile the test report.</li>
 *   <li><b>Verify</b> - Run scenario-specific verification checks (e.g. completion counts, relation counts).</li>
 * </ol>
 * On failure, stats are still collected and attached to the test run for diagnostics.
 */
@Component
@Slf4j
public class PerfTestHarness {

    private final Map<TestScenarioType, TestScenario> scenarios;

    public PerfTestHarness(List<TestScenario> scenarioImplementations) {
        this.scenarios = scenarioImplementations.stream()
                .collect(Collectors.toMap(TestScenario::getScenarioType, Function.identity()));
    }

    /**
     * Executes a full test run through all lifecycle phases, updating the {@link TestRun} status
     * as it progresses.
     */
    public void execute(TestRun testRun) {
        TestScenario scenario = resolveScenario(testRun.getScenario());
        try {
            log.info("Preparing scenario {}", scenario);
            testRun.clearDatabase();
            scenario.prepare(testRun);

            log.info("Warming up...");
            testRun.startWarmUp();
            scenario.warmUp(testRun);
            testRun.reset();

            log.info("Running scenario...");
            testRun.start();
            scenario.run(testRun);

            log.info("Scenario run completed");
            testRun.complete();

            log.info("Collection test report...");
            var testReport = testRun.collectStats();
            scenario.verify(testRun);

            log.info("Test run {} completed successfully", testRun.getId());
            log.info(testReport.formatted());
        } catch (Exception e) {
            log.error("Test run {} failed", testRun.getId(), e);
            testRun.fail(e.getMessage());

            var testReport = testRun.collectStats();
            testRun.setReport(testReport);
        }
    }

    private TestScenario resolveScenario(TestScenarioType scenarioType) {
        TestScenario scenario = scenarios.get(scenarioType);
        if (scenario == null) {
            throw new IllegalArgumentException("No implementation found for scenario: " + scenarioType);
        }
        return scenario;
    }
}
