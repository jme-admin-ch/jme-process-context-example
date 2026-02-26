package ch.admin.bit.jeap.jme.processcontext.perftest.report;

import ch.admin.bit.jeap.jme.processcontext.perftest.TestProcessInstance;
import ch.admin.bit.jeap.jme.processcontext.perftest.TestRun;
import ch.admin.bit.jeap.jme.processcontext.perftest.TestScenarioType;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SameParameterValue")
class TimeSeriesTest {

    private static final LocalDateTime START = LocalDateTime.parse("2026-02-24T09:00:00");
    private static final LocalDateTime END = LocalDateTime.parse("2026-02-24T10:00:00");

    @Test
    void of_creates100Buckets() {
        TestRun testRun = testRun();

        TimeSeries timeSeries = TimeSeries.of(testRun);

        assertEquals(100, timeSeries.buckets().size());
    }

    @Test
    void of_bucketsHaveCorrectTimestamps() {
        TestRun testRun = testRun();

        TimeSeries timeSeries = TimeSeries.of(testRun);

        // 1 hour = 3_600_000 ms, bucket duration = 36_000 ms = 36 s
        assertEquals(START, timeSeries.buckets().get(0).getTs());
        assertEquals(START.plusSeconds(36), timeSeries.buckets().get(1).getTs());
        assertEquals(START.plusSeconds(72), timeSeries.buckets().get(2).getTs());
    }

    @Test
    void of_sortsInstanceIntoCorrectBucket() {
        // Instance created 1 minute after start -> bucket index = 60_000 / 36_000 = 1
        TestRun testRun = testRun();
        testRun.recordCreatedProcess(processInstance(START.plusMinutes(1)));

        TimeSeries timeSeries = TimeSeries.of(testRun);

        assertEquals(0, timeSeries.buckets().get(0).getCreated());
        assertEquals(1, timeSeries.buckets().get(1).getCreated());
    }

    @Test
    void of_instanceAtStartGoesToFirstBucket() {
        TestRun testRun = testRun();
        testRun.recordCreatedProcess(processInstance(START));

        TimeSeries timeSeries = TimeSeries.of(testRun);

        assertEquals(1, timeSeries.buckets().getFirst().getCreated());
    }

    @Test
    void of_multipleInstancesInSameBucket() {
        TestRun testRun = testRun();
        testRun.recordCreatedProcess(processInstance(START.plusSeconds(1)));
        testRun.recordCreatedProcess(processInstance(START.plusSeconds(10)));
        testRun.recordCreatedProcess(processInstance(START.plusSeconds(35)));

        TimeSeries timeSeries = TimeSeries.of(testRun);

        assertEquals(3, timeSeries.buckets().getFirst().getCreated());
    }

    @Test
    void of_instancesDistributedAcrossBuckets() {
        TestRun testRun = testRun();
        testRun.recordCreatedProcess(processInstance(START));                    // bucket 0
        testRun.recordCreatedProcess(processInstance(START.plusMinutes(30)));     // bucket 50
        testRun.recordCreatedProcess(processInstance(START.plusMinutes(59)));     // bucket 98

        TimeSeries timeSeries = TimeSeries.of(testRun);

        assertEquals(1, timeSeries.buckets().getFirst().getCreated());
        assertEquals(1, timeSeries.buckets().get(50).getCreated());
        assertEquals(1, timeSeries.buckets().get(98).getCreated());

        long totalCreated = timeSeries.buckets().stream().mapToLong(Bucket::getCreated).sum();
        assertEquals(3, totalCreated);
    }

    @Test
    void of_emptyBucketsHaveZeroCreated() {
        TestRun testRun = testRun();
        testRun.recordCreatedProcess(processInstance(START));

        TimeSeries timeSeries = TimeSeries.of(testRun);

        for (int i = 1; i < 100; i++) {
            assertEquals(0, timeSeries.buckets().get(i).getCreated(),
                    "Bucket " + i + " should have 0 created");
        }
    }

    @Test
    void of_sortsCompletedInstanceIntoCorrectBucket() {
        TestRun testRun = testRun();
        // Created at start, completed 1 minute later -> completion bucket = 60_000 / 36_000 = 1
        testRun.recordCreatedProcess(completedProcessInstance(START, START.plusMinutes(1)));

        TimeSeries timeSeries = TimeSeries.of(testRun);

        assertEquals(1, timeSeries.buckets().get(0).getCreated());
        assertEquals(0, timeSeries.buckets().get(0).getCompleted());
        assertEquals(0, timeSeries.buckets().get(1).getCreated());
        assertEquals(1, timeSeries.buckets().get(1).getCompleted());
    }

    @Test
    void of_multipleCompletionsInSameBucket() {
        TestRun testRun = testRun();
        testRun.recordCreatedProcess(completedProcessInstance(START, START.plusSeconds(1)));
        testRun.recordCreatedProcess(completedProcessInstance(START, START.plusSeconds(10)));
        testRun.recordCreatedProcess(completedProcessInstance(START, START.plusSeconds(35)));

        TimeSeries timeSeries = TimeSeries.of(testRun);

        assertEquals(3, timeSeries.buckets().getFirst().getCompleted());
    }

    @Test
    void of_tracksCreationDurationPerBucket() {
        TestRun testRun = testRun();
        // Two instances created in bucket 0 with different creation durations
        testRun.recordCreatedProcess(processInstanceWithCreationDuration(START, Duration.ofMillis(100)));
        testRun.recordCreatedProcess(processInstanceWithCreationDuration(START.plusSeconds(1), Duration.ofMillis(300)));

        TimeSeries timeSeries = TimeSeries.of(testRun);

        DurationAggregate agg = timeSeries.buckets().getFirst().getCreationDuration();
        assertEquals(2, agg.getCount());
        assertEquals(100, agg.getMin());
        assertEquals(300, agg.getMax());
        assertEquals(200, agg.getAvg());
    }

    @Test
    void of_tracksCompletionDurationPerBucket() {
        TestRun testRun = testRun();
        // Created at start, completed at +1min with different completion durations
        testRun.recordCreatedProcess(completedProcessInstanceWithDuration(START, START.plusMinutes(1), Duration.ofMillis(500)));
        testRun.recordCreatedProcess(completedProcessInstanceWithDuration(START, START.plusMinutes(1).plusSeconds(1), Duration.ofMillis(900)));

        TimeSeries timeSeries = TimeSeries.of(testRun);

        // Completion bucket = 60_000 / 36_000 = 1
        DurationAggregate agg = timeSeries.buckets().get(1).getCompletionDuration();
        assertEquals(2, agg.getCount());
        assertEquals(500, agg.getMin());
        assertEquals(900, agg.getMax());
        assertEquals(700, agg.getAvg());
    }

    @Test
    void of_emptyBucketHasZeroDurations() {
        TestRun testRun = testRun();

        TimeSeries timeSeries = TimeSeries.of(testRun);

        DurationAggregate agg = timeSeries.buckets().getFirst().getCreationDuration();
        assertEquals(0, agg.getCount());
        assertEquals(0, agg.getMin());
        assertEquals(0, agg.getMax());
        assertEquals(0, agg.getAvg());
    }

    @Test
    void of_incompletedInstanceHasNoCompletionBucket() {
        TestRun testRun = testRun();
        testRun.recordCreatedProcess(processInstance(START));

        TimeSeries timeSeries = TimeSeries.of(testRun);

        long totalCompleted = timeSeries.buckets().stream().mapToLong(Bucket::getCompleted).sum();
        assertEquals(0, totalCompleted);
    }

    private static TestRun testRun() {
        TestRun testRun = new TestRun(null, UUID.randomUUID(), TestScenarioType.HIGH_MESSAGE_COUNT, Map.of(), Duration.ZERO, true);
        ReflectionTestUtils.setField(testRun, "startedAt", START);
        ReflectionTestUtils.setField(testRun, "finishedAt", END);
        return testRun;
    }

    private static TestProcessInstance processInstance(LocalDateTime createdAt) {
        TestProcessInstance instance = new TestProcessInstance("test-" + UUID.randomUUID(), createdAt);
        instance.createdAt(createdAt);
        return instance;
    }

    private static TestProcessInstance completedProcessInstance(LocalDateTime createdAt, LocalDateTime completedAt) {
        TestProcessInstance instance = processInstance(createdAt);
        instance.completed(completedAt);
        return instance;
    }

    private static TestProcessInstance processInstanceWithCreationDuration(LocalDateTime createdAt, Duration creationDuration) {
        // firstEventCreatedAt is set such that creationDuration = createdAt - firstEventCreatedAt
        TestProcessInstance instance = new TestProcessInstance(
                "test-" + UUID.randomUUID(),
                createdAt.minus(creationDuration));
        instance.createdAt(createdAt);
        return instance;
    }

    private static TestProcessInstance completedProcessInstanceWithDuration(LocalDateTime createdAt, LocalDateTime completedAt, Duration completionDuration) {
        // completionDuration = completedAt - processCreatedAt, so processCreatedAt = completedAt - completionDuration
        LocalDateTime actualCreatedAt = completedAt.minus(completionDuration);
        TestProcessInstance instance = new TestProcessInstance(
                "test-" + UUID.randomUUID(),
                createdAt);
        instance.createdAt(actualCreatedAt);
        instance.completed(completedAt);
        return instance;
    }
}
