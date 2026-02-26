package ch.admin.bit.jeap.jme.processcontext.perftest.report;

import ch.admin.bit.jeap.jme.processcontext.perftest.TestProcessInstance;
import ch.admin.bit.jeap.jme.processcontext.perftest.TestRun;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;

class DurationAggregate {
    private long min = Long.MAX_VALUE;
    private long max = 0;
    private long sum = 0;
    @Getter
    private long count = 0;

    void record(long durationMs) {
        min = Math.min(min, durationMs);
        max = Math.max(max, durationMs);
        sum += durationMs;
        count++;
    }

    long getMin() {
        return count > 0 ? min : 0;
    }

    long getMax() {
        return max;
    }

    long getAvg() {
        return count > 0 ? sum / count : 0;
    }
}

class Bucket {
    @Getter
    private final LocalDateTime ts;
    @Getter
    private long created = 0;
    @Getter
    private long completed = 0;
    @Getter
    private final DurationAggregate creationDuration = new DurationAggregate();
    @Getter
    private final DurationAggregate completionDuration = new DurationAggregate();

    Bucket(LocalDateTime ts) {
        this.ts = ts;
    }

    void incrementCreated() {
        created++;
    }

    void incrementCompleted() {
        completed++;
    }

    void recordCreationDuration(long durationMs) {
        creationDuration.record(durationMs);
    }

    void recordCompletionDuration(long durationMs) {
        completionDuration.record(durationMs);
    }
}

/**
 * Splits the test run duration into {@value #BUCKETS} equal time buckets and sorts each
 * process instance into its creation and completion bucket. Each bucket tracks counts and
 * duration aggregates (min/max/avg) for both creation delay and completion duration,
 * providing the data for the time series charts in the HTML report.
 */
record TimeSeries(List<Bucket> buckets) {

    static final int BUCKETS = 100;

    static TimeSeries of(TestRun testRun) {
        if (testRun.getStartedAt() == null || testRun.getFinishedAt() == null) {
            return new TimeSeries(List.of());
        }

        LocalDateTime start = testRun.getStartedAt();
        LocalDateTime end = testRun.getFinishedAt();
        long totalDuration = start.until(end, ChronoUnit.MILLIS);
        long bucketDuration = Math.max(1, totalDuration / BUCKETS);

        List<Bucket> buckets = createBuckets(start, bucketDuration);

        var processInstances = testRun.getProcessInstances();
        sortIntoBuckets(start, bucketDuration, buckets, processInstances);

        return new TimeSeries(buckets);
    }

    private static List<Bucket> createBuckets(LocalDateTime start, long bucketDuration) {
        return IntStream.range(0, BUCKETS)
                .mapToObj(i -> new Bucket(start.plus(bucketDuration * i, ChronoUnit.MILLIS)))
                .toList();
    }

    private static void sortIntoBuckets(LocalDateTime start, long bucketDuration, List<Bucket> buckets, List<TestProcessInstance> processInstances) {
        for (TestProcessInstance processInstance : processInstances) {
            int creationBucket = bucket(start, processInstance.getProcessCreatedAt(), bucketDuration);
            buckets.get(creationBucket).incrementCreated();
            if (processInstance.getCreationDelay() != null) {
                buckets.get(creationBucket).recordCreationDuration(processInstance.getCreationDelay().toMillis());
            }
            if (processInstance.isCompleted()) {
                int completionBucket = bucket(start, processInstance.getProcessCompletedAt(), bucketDuration);
                if (completionBucket < BUCKETS) {
                    buckets.get(completionBucket).incrementCompleted();
                    buckets.get(completionBucket).recordCompletionDuration(processInstance.getCompletionDuration().toMillis());
                }
            }
        }
    }

    private static int bucket(LocalDateTime start, LocalDateTime ts, long bucketDuration) {
        long tsDuration = start.until(ts, ChronoUnit.MILLIS);
        if (tsDuration < 0) {
            return 0;
        }
        return (int) (tsDuration / bucketDuration);
    }
}
