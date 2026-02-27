package ch.admin.bit.jeap.jme.processcontext.perftest.load;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 * Generates concurrent load by distributing work items across a fixed thread pool.
 * Supports two modes:
 * <ul>
 *   <li><b>Burst</b> ({@link #generateBurst}) - executes all items as fast as possible.</li>
 *   <li><b>Rate-limited</b> ({@link #generateDistributedOverTime}) - spreads items evenly over a
 *       given duration, with periodic progress logging.</li>
 * </ul>
 * Work is partitioned across threads in a round-robin fashion (thread 0 handles items 0, N, 2N, ...;
 * thread 1 handles items 1, N+1, 2N+1, ...; etc.) to ensure even distribution.
 */
@Component
@Slf4j
public class LoadGenerator {

    @Value("${jeap.pcs.loadgenerator.concurrency:5}")
    private int concurrency = 5;

    /**
     * Executes all {@code count} items as fast as possible using up to {@code concurrency} threads.
     */
    public void generateBurst(int count, IntConsumer task) {
        log.info("Generating load with concurrency: {}, count: {}", concurrency, count);
        int threads = Math.min(concurrency, count);
        AtomicInteger actualCount = new AtomicInteger(0);
        distribute(count, threads, task, actualCount);
        log.info("Load generation completed, actual count: {}", actualCount.get());
    }

    /**
     * Spreads {@code count} items evenly over the given {@code duration}. Each item is scheduled at
     * a fixed interval and threads sleep until their next scheduled time. Falls back to burst mode
     * if the duration is zero or negative. Logs progress every 15 seconds.
     */
    public void generateDistributedOverTime(int count, Duration duration, IntConsumer task) {
        if (duration.isZero() || duration.isNegative()) {
            generateBurst(count, task);
            return;
        }

        double rps = (double) count / duration.toSeconds();
        log.info("Generating rate-limited load with concurrency: {}, count: {}, duration: {}, target rps: {}", concurrency, count, duration, rps);
        int threads = Math.min(concurrency, count);
        double intervalMs = (double) duration.toMillis() / count;
        long startTime = System.nanoTime();
        AtomicInteger actualCount = new AtomicInteger(0);
        try (var _ = startProgressLogger(count, actualCount)) {
            distribute(count, threads, j -> {
                sleepUntilScheduled(startTime, j, intervalMs);
                task.accept(j);
            }, actualCount);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
        log.info("Rate-limited load generation completed, actual count: {}, elapsed: {}ms", actualCount.get(), elapsedMs);
    }

    private static AutoCloseable startProgressLogger(int count, AtomicInteger actualCount) {
        ScheduledExecutorService progressLogger = Executors.newSingleThreadScheduledExecutor();
        progressLogger.scheduleAtFixedRate(
                () -> log.info("Load generation progress: {}/{} ({}%)", actualCount.get(), count, actualCount.get() * 100 / count),
                15, 15, TimeUnit.SECONDS);
        return progressLogger::shutdownNow;
    }

    private void distribute(int count, int threads, IntConsumer itemTask, AtomicInteger actualCount) {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            var futures = IntStream.range(0, threads)
                    .mapToObj(threadIndex -> executor.submit(
                            () -> executePartition(threadIndex, count, threads, itemTask, actualCount)))
                    .toList();
            awaitAll(futures);
        } catch (RuntimeException e) {
            executor.shutdownNow();
            throw e;
        } finally {
            executor.close();
        }
    }

    private static void executePartition(int threadIndex, int count, int threads,
                                         IntConsumer itemTask, AtomicInteger actualCount) {
        for (int j = threadIndex; j < count; j += threads) {
            if (Thread.currentThread().isInterrupted()) {
                throw new RuntimeException("Load generation interrupted");
            }
            itemTask.accept(j);
            actualCount.incrementAndGet();
        }
    }

    private static void sleepUntilScheduled(long startNanos, int itemIndex, double intervalMs) {
        if (Thread.currentThread().isInterrupted()) {
            throw new RuntimeException("Rate-limited load generation interrupted");
        }
        long scheduledNanos = startNanos + (long) (itemIndex * intervalMs * 1_000_000);
        long sleepNanos = scheduledNanos - System.nanoTime();
        if (sleepNanos > 0) {
            try {
                Thread.sleep(sleepNanos / 1_000_000, (int) (sleepNanos % 1_000_000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Rate-limited load generation interrupted", e);
            }
        }
    }

    private static void awaitAll(List<? extends Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Load generation interrupted", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Load generation failed", e.getCause());
            }
        }
    }
}
