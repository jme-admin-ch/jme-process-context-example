package ch.admin.bit.jeap.jme.processcontext.perftest;

import ch.admin.bit.jeap.jme.processcontext.perftest.report.HtmlTestReporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor
@Slf4j
public class PerfTestService {

    private final PerfTestHarness harness;
    private final TestProcessInstanceRepository testProcessInstanceRepository;
    private final AsyncTaskExecutor taskExecutor;
    private final Map<UUID, TestRun> testRuns = new ConcurrentHashMap<>();
    private final Map<UUID, Future<?>> testRunFutures = new ConcurrentHashMap<>();

    /**
     * Creates and starts a new test run for the given scenario, executing it asynchronously.
     * The test run is stored in an in-memory map for later retrieval of its status and results.
     */
    public void startTestAsync(UUID id, TestScenarioType scenario, Map<String, Object> parameters, Duration timeout, boolean clearDatabase) {
        TestRun testRun = new TestRun(testProcessInstanceRepository, id, scenario, parameters, timeout, clearDatabase);
        testRuns.put(id, testRun);
        log.info("Created test run {} for scenario {} with parameters {}", testRun.getId(), scenario, parameters);
        Future<?> future = taskExecutor.submit(() -> {
            harness.execute(testRun);
            testRun.reset(); // clear process instance and message data from run
        });
        testRunFutures.put(id, future);
    }

    public List<TestRun> getAllTestRuns() {
        return testRuns.values().stream()
                .sorted(Comparator.comparing(TestRun::getCreatedAt).reversed())
                .toList();
    }

    public boolean cancelTestRun(UUID testRunId) {
        TestRun testRun = testRuns.get(testRunId);
        if (testRun == null || testRun.isTerminal()) {
            return false;
        }
        Future<?> future = testRunFutures.get(testRunId);
        if (future != null) {
            future.cancel(true);
        }
        testRun.cancel();
        log.info("Cancelled test run {}", testRunId);
        return true;
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
