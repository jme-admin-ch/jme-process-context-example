package ch.admin.bit.jeap.jme.processcontext.perftest.report;

import ch.admin.bit.jeap.jme.processcontext.perftest.TestRunStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlTestReporterTest {

    @ParameterizedTest
    @EnumSource(TestRunStatus.class)
    void generateReport_containsAllResolvedFields(TestRunStatus status) {
        TestReport report = buildTestReport(status);

        String html = HtmlTestReporter.generateReport(report);

        // Status is resolved
        assertTrue(html.contains(status.name()),
                "Expected status " + status.name() + " in rendered HTML");

        // No unresolved Thymeleaf expressions
        assertFalse(html.contains("th:text"),
                "Thymeleaf th:text attributes should be fully resolved");

        // Datetime fields are rendered without 'T' infix
        assertTrue(html.contains("2026-02-24 08:55:00"),
                "Warm-up start time should be present");
        assertTrue(html.contains("2026-02-24 09:00:00"),
                "Start time should be present");
        assertTrue(html.contains("2026-02-24 10:00:00"),
                "Finish time should be present");

        // Duration fields in seconds
        assertTrue(html.contains("300 s"),
                "Warm-up duration should be present");
        assertTrue(html.contains("3600 s"),
                "Total run duration should be present");

        // Process instance counts and messages
        assertTrue(html.contains("100"),
                "Process instances created should be present");
        assertTrue(html.contains("95"),
                "Process instances completed should be present");
        assertTrue(html.contains("1234"),
                "Total messages should be present");

        // Creation stats
        assertTrue(html.contains("50 ms"),
                "Creation min duration should be present");
        assertTrue(html.contains("500 ms"),
                "Creation max duration should be present");
        assertTrue(html.contains("250 ms"),
                "Creation avg duration should be present");
        assertTrue(html.contains("230 ms"),
                "Creation median duration should be present");

        // Completion stats
        assertTrue(html.contains("100 ms"),
                "Completion min duration should be present");
        assertTrue(html.contains("2000 ms"),
                "Completion max duration should be present");
        assertTrue(html.contains("800 ms"),
                "Completion avg duration should be present");
        assertTrue(html.contains("750 ms"),
                "Completion median duration should be present");

        // Creation time series chart data
        assertTrue(html.contains("creationTimeSeries"),
                "Creation time series variable should be present");
        assertTrue(html.contains("2026-02-24T09:00"),
                "First bucket timestamp should be present");

        // Completion time series chart data
        assertTrue(html.contains("completionTimeSeries"),
                "Completion time series variable should be present");

        // Verify bucket values are present (creation: 5,3 and completion: 2,4)
        assertTrue(html.contains("\"value\":5"),
                "Creation bucket with 5 should be present");
        assertTrue(html.contains("\"value\":3"),
                "Creation bucket with 3 should be present");
        assertTrue(html.contains("\"value\":2"),
                "Completion bucket with 2 should be present");
        assertTrue(html.contains("\"value\":4"),
                "Completion bucket with 4 should be present");

        // Duration time series chart data
        assertTrue(html.contains("creationDurationTimeSeries"),
                "Creation delay time series variable should be present");
        assertTrue(html.contains("completionDurationTimeSeries"),
                "Completion duration time series variable should be present");
        assertTrue(html.contains("\"min\":"),
                "Duration min values should be present");
        assertTrue(html.contains("\"avg\":"),
                "Duration avg values should be present");
        assertTrue(html.contains("\"max\":"),
                "Duration max values should be present");

        assertFalse(html.contains("th:inline"),
                "Thymeleaf th:inline attributes should be fully resolved");
    }

    private static TestReport buildTestReport(TestRunStatus status) {
        LocalDateTime warmUpStart = LocalDateTime.parse("2026-02-24T08:55:00");
        LocalDateTime start = LocalDateTime.parse("2026-02-24T09:00:00");
        LocalDateTime finish = LocalDateTime.parse("2026-02-24T10:00:00");

        Stats creationStats = Stats.builder()
                .minDuration(Duration.ofMillis(50))
                .maxDuration(Duration.ofMillis(500))
                .avgDuration(Duration.ofMillis(250))
                .medianDuration(Duration.ofMillis(230))
                .build();

        Stats completionStats = Stats.builder()
                .minDuration(Duration.ofMillis(100))
                .maxDuration(Duration.ofMillis(2000))
                .avgDuration(Duration.ofMillis(800))
                .medianDuration(Duration.ofMillis(750))
                .build();

        Bucket bucket1 = new Bucket(start);
        for (int i = 0; i < 5; i++) {
            bucket1.incrementCreated();
        }
        for (int i = 0; i < 2; i++) {
            bucket1.incrementCompleted();
        }
        bucket1.recordCreationDuration(150);
        bucket1.recordCreationDuration(250);
        bucket1.recordCompletionDuration(400);
        bucket1.recordCompletionDuration(600);
        Bucket bucket2 = new Bucket(start.plusMinutes(30));
        for (int i = 0; i < 3; i++) {
            bucket2.incrementCreated();
        }
        for (int i = 0; i < 4; i++) {
            bucket2.incrementCompleted();
        }
        bucket2.recordCreationDuration(300);
        bucket2.recordCompletionDuration(800);
        TimeSeries timeSeries = new TimeSeries(List.of(bucket1, bucket2));

        return TestReport.builder()
                .status(status)
                .parameters(Map.of("processCount", 100, "durationSeconds", 60))
                .warmUpStartedAt(warmUpStart)
                .startedAt(start)
                .finishedAt(finish)
                .warmUpDuration(Duration.ofMinutes(5))
                .totalRunDuration(Duration.ofHours(1))
                .processInstancesCreated(100)
                .processInstancesCompleted(95)
                .messageThroughput(1234.0 / 3600)
                .creationThroughput(100.0 / 3600)
                .creationStats(creationStats)
                .completionStats(completionStats)
                .statsTimeSeries(timeSeries)
                .totalMessages(1234)
                .messages(List.of("All 95 process instances completed", "Relation count: 190"))
                .build();
    }
}
