package ch.admin.bit.jeap.jme.processcontext.perftest;

import ch.admin.bit.jeap.jme.processcontext.perftest.report.TestReport;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class TestRun {

    @Getter(AccessLevel.NONE)
    private final TestProcessInstanceRepository testProcessInstanceRepository;
    private final UUID id;
    private final TestScenarioType scenario;
    private final LocalDateTime createdAt;
    private final List<TestProcessInstance> processInstances = new CopyOnWriteArrayList<>();
    private final Duration timeout;
    private final Map<String, Object> parameters;
    private final boolean clearDatabase;

    private TestRunStatus status;
    private LocalDateTime warmUpStartedAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private TestReport testReport;
    private String errorMessage;
    private int messageCount = 0;

    public TestRun(TestProcessInstanceRepository testProcessInstanceRepository, UUID id, TestScenarioType scenario,
                   Map<String, Object> parameters, Duration timeout, boolean clearDatabase) {
        this.parameters = parameters;
        this.timeout = timeout;
        this.testProcessInstanceRepository = testProcessInstanceRepository;
        this.id = id;
        this.scenario = scenario;
        this.clearDatabase = clearDatabase;
        this.createdAt = LocalDateTime.now();
        this.status = TestRunStatus.PREPARING;
    }

    void startWarmUp() {
        this.warmUpStartedAt = LocalDateTime.now();
        this.status = TestRunStatus.WARMING_UP;
    }

    void start() {
        this.startedAt = LocalDateTime.now();
        this.status = TestRunStatus.RUNNING;
    }

    void complete() {
        this.finishedAt = LocalDateTime.now();
        this.status = TestRunStatus.COMPLETED;
    }

    void fail(String errorMessage) {
        this.finishedAt = LocalDateTime.now();
        this.status = TestRunStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void recordCreatedProcess(TestProcessInstance processInstance) {
        processInstances.add(processInstance);
    }

    public void waitUntilAllProcessesCompleted() {
        testProcessInstanceRepository.waitUntilAllProcessesCompleted(processInstances, timeout);
    }

    TestReport collectStats() {
        testProcessInstanceRepository.collectProcessState(processInstances);
        var report = TestReport.withStats(this);
        this.testReport = report;
        return report;
    }

    public void clearDatabase() {
        if (clearDatabase) {
            testProcessInstanceRepository.clearDatabase();
        }
    }

    public List<TestProcessInstance> getProcessInstances() {
        return Collections.unmodifiableList(processInstances);
    }

    public void setReport(TestReport testReport) {
        this.testReport = testReport;
    }

    public void reset() {
        this.messageCount = 0;
        this.processInstances.clear();
    }

    public synchronized void incrementMessageCount() {
        this.messageCount++;
    }

    public int getParameterOrDefault(String parameterName, int defaultValue) {
        return (int) Objects.requireNonNullElse(parameters.get(parameterName), defaultValue);
    }

    public void assertRelationCount(int expectedRelationCount) {
        int totalRelationCount = testProcessInstanceRepository.getTotalRelationCount(processInstances);
        testReport.addMessage("Total relation count: %d, expected relation count: %d"
                .formatted(totalRelationCount, expectedRelationCount));
        if (totalRelationCount != expectedRelationCount) {
            throw new IllegalStateException("Expected %d relations in total, but found %d".formatted(expectedRelationCount, totalRelationCount));
        }
    }
}
