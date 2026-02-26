package ch.admin.bit.jeap.jme.processcontext.perftest;

import ch.admin.bit.jeap.jme.processcontext.perftest.report.HtmlTestReporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class PerfTestService {

    private final PerfTestHarness harness;
    private final TestProcessInstanceRepository testProcessInstanceRepository;
    private final Map<UUID, TestRun> testRuns = new ConcurrentHashMap<>();

    /**
     * Creates and starts a new test run for the given scenario, executing it asynchronously.
     * The test run is stored in an in-memory map for later retrieval of its status and results.
     */
    @Async
    public void startTestAsync(UUID id, TestScenarioType scenario, Map<String, Object> parameters, Duration timeout, boolean clearDatabase) {
        TestRun testRun = new TestRun(testProcessInstanceRepository, id, scenario, parameters, timeout, clearDatabase);
        testRuns.put(id, testRun);
        log.info("Created test run {} for scenario {} with parameters {}", testRun.getId(), scenario, parameters);
        harness.execute(testRun);
        testRun.reset(); // clear process instance and message data from run
    }

    public Optional<TestRun> getTestRun(UUID testRunId) {
        return Optional.ofNullable(testRuns.get(testRunId));
    }

    public Optional<String> getTestReport(UUID testRunId) {
        return getTestRun(testRunId)
                .map(TestRun::getTestReport)
                .map(HtmlTestReporter::generateReport);
    }

    public Optional<UUID> getLatestTestRunId() {
        return testRuns.values().stream()
                .max(Comparator.comparing(TestRun::getCreatedAt))
                .map(TestRun::getId);
    }
}
