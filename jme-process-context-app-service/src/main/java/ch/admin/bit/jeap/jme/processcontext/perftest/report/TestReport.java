package ch.admin.bit.jeap.jme.processcontext.perftest.report;

import ch.admin.bit.jeap.jme.processcontext.perftest.TestProcessInstance;
import ch.admin.bit.jeap.jme.processcontext.perftest.TestRun;
import ch.admin.bit.jeap.jme.processcontext.perftest.TestRunStatus;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Value
@Builder
@Slf4j
public class TestReport {
    String scenario;
    Map<String, Object> parameters;
    TestRunStatus status;
    LocalDateTime warmUpStartedAt;
    LocalDateTime startedAt;
    LocalDateTime finishedAt;
    long processInstancesCreated;
    long processInstancesCompleted;
    Duration totalRunDuration;
    Duration warmUpDuration;
    double messageThroughput;
    double creationThroughput;
    Stats creationStats;
    Stats completionStats;
    TimeSeries statsTimeSeries;
    int totalMessages;
    String failureMessage;
    @Builder.Default
    List<String> messages = new ArrayList<>();

    public static TestReport withStats(TestRun testRun) {
        var processInstances = testRun.getProcessInstances();
        long completionCount = countCompleted(processInstances);
        Duration totalRunDuration = durationBetween(testRun.getStartedAt(), testRun.getFinishedAt());
        Duration creationSpan = durationBetween(testRun.getStartedAt(), lastCreationTimestamp(processInstances));
        return TestReport.builder()
                .scenario(testRun.getScenario().getLabel() + ": " + testRun.getScenario().getDescription())
                .parameters(testRun.getParameters())
                .status(testRun.getStatus())
                .warmUpStartedAt(testRun.getWarmUpStartedAt())
                .startedAt(testRun.getStartedAt())
                .finishedAt(testRun.getFinishedAt())
                .warmUpDuration(durationBetween(testRun.getWarmUpStartedAt(), testRun.getStartedAt()))
                .totalRunDuration(totalRunDuration)
                .processInstancesCreated(processInstances.size())
                .processInstancesCompleted(completionCount)
                .messageThroughput(throughput(testRun.getMessageCount(), totalRunDuration))
                .creationThroughput(throughput(processInstances.size(), creationSpan))
                .creationStats(buildCreationStats(processInstances))
                .completionStats(buildCompletionStats(processInstances, completionCount))
                .statsTimeSeries(TimeSeries.of(testRun))
                .totalMessages(testRun.getMessageCount())
                .failureMessage(testRun.getErrorMessage())
                .build();
    }

    private static Duration durationBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return Duration.ZERO;
        }
        return Duration.between(from, to);
    }

    private static double throughput(long count, Duration duration) {
        long seconds = duration.toSeconds();
        return seconds > 0 ? (double) count / seconds : 0;
    }

    private static LocalDateTime lastCreationTimestamp(List<TestProcessInstance> processInstances) {
        return processInstances.stream()
                .map(TestProcessInstance::getProcessCreatedAt)
                .filter(java.util.Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private static Stats buildCreationStats(List<TestProcessInstance> processInstances) {
        return Stats.builder()
                .minDuration(minDuration(processInstances, TestProcessInstance::getCreationDelay))
                .maxDuration(maxDuration(processInstances, TestProcessInstance::getCreationDelay))
                .avgDuration(avgDuration(processInstances.size(), processInstances, TestProcessInstance::getCreationDelay))
                .medianDuration(medianDuration(processInstances, TestProcessInstance::getCreationDelay))
                .build();
    }

    private static Stats buildCompletionStats(List<TestProcessInstance> processInstances, long completionCount) {
        return Stats.builder()
                .minDuration(minDuration(processInstances, TestProcessInstance::getCompletionDuration))
                .maxDuration(maxDuration(processInstances, TestProcessInstance::getCompletionDuration))
                .avgDuration(avgDuration(completionCount, processInstances, TestProcessInstance::getCompletionDuration))
                .medianDuration(medianDuration(processInstances, TestProcessInstance::getCompletionDuration))
                .build();
    }

    private static Duration minDuration(List<TestProcessInstance> processInstances, Function<TestProcessInstance, Duration> function) {
        return processInstances.stream()
                .filter(TestProcessInstance::isCompleted)
                .map(function)
                .min(Duration::compareTo)
                .orElse(Duration.ZERO);
    }

    private static Duration maxDuration(List<TestProcessInstance> processInstances, Function<TestProcessInstance, Duration> function) {
        return processInstances.stream()
                .filter(TestProcessInstance::isCompleted)
                .map(function)
                .max(Duration::compareTo)
                .orElse(Duration.ZERO);
    }

    private static Duration avgDuration(long completionCount, List<TestProcessInstance> processInstances, Function<TestProcessInstance, Duration> function) {
        if (completionCount == 0) {
            return Duration.ZERO;
        }
        Duration totalDuration = processInstances.stream()
                .filter(TestProcessInstance::isCompleted)
                .map(function)
                .reduce(Duration.ZERO, Duration::plus);
        return totalDuration.dividedBy(completionCount);
    }

    private static Duration medianDuration(List<TestProcessInstance> processInstances, Function<TestProcessInstance, Duration> function) {
        List<Duration> durations = processInstances.stream()
                .filter(TestProcessInstance::isCompleted)
                .map(function)
                .sorted()
                .toList();
        int size = durations.size();
        if (size == 0) {
            return Duration.ZERO;
        }
        if (size % 2 == 1) {
            return durations.get(size / 2);
        } else {
            Duration d1 = durations.get(size / 2 - 1);
            Duration d2 = durations.get(size / 2);
            return d1.plus(d2).dividedBy(2);
        }
    }

    private static long countCompleted(List<TestProcessInstance> processInstances) {
        return processInstances.stream()
                .filter(TestProcessInstance::isCompleted)
                .count();
    }

    public String formatted() {
        return String.format("""
                        Test Report:
                        Status: %s
                        Warmup started at: %s
                        Started at: %s
                        Finished at: %s
                        Warm up duration: %sms
                        Total run duration: %sms
                        Process instances created: %d
                        Process instances completed: %d
                        Process instance complete percentage: %d
                        Message throughput: %.2f msg/sec
                        Creation throughput: %.2f processes/sec
                        Process creation delay stats:
                        %s
                        Process completion duration stats:
                        %s
                        """,
                status,
                warmUpStartedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                startedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                finishedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                warmUpDuration.toMillis(),
                totalRunDuration.toMillis(),
                processInstancesCreated, processInstancesCompleted, percentageCompleted(),
                messageThroughput, creationThroughput,
                creationStats.formatted(), completionStats.formatted());
    }

    private int percentageCompleted() {
        return (int) (Math.round((double) processInstancesCreated / processInstancesCompleted * 100.0));
    }

    public void addMessage(String msg) {
        log.info(msg);
        this.messages.add(msg);
    }
}
