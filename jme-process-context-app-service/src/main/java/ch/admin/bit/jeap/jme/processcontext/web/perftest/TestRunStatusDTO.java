package ch.admin.bit.jeap.jme.processcontext.web.perftest;

import ch.admin.bit.jeap.jme.processcontext.perftest.TestRun;
import ch.admin.bit.jeap.jme.processcontext.perftest.TestRunStatus;
import ch.admin.bit.jeap.jme.processcontext.perftest.TestScenarioType;
import ch.admin.bit.jeap.jme.processcontext.perftest.report.TestReport;
import lombok.Value;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Value
public class TestRunStatusDTO {
    UUID testRunId;
    TestScenarioType scenario;
    TestRunStatus status;
    LocalDateTime createdAt;
    LocalDateTime startedAt;
    LocalDateTime finishedAt;
    int messageCount;
    int createdProcessInstanceCount;
    Map<String, Object> parameters;
    Duration timeout;
    String errorMessage;
    TestReport testReport;

    public static TestRunStatusDTO fromTestRun(TestRun testRun) {
        return new TestRunStatusDTO(
                testRun.getId(),
                testRun.getScenario(),
                testRun.getStatus(),
                testRun.getCreatedAt(),
                testRun.getStartedAt(),
                testRun.getFinishedAt(),
                testRun.getMessageCount(),
                testRun.getProcessInstances().size(),
                testRun.getParameters(),
                testRun.getTimeout(),
                testRun.getErrorMessage(),
                testRun.getTestReport());
    }
}
