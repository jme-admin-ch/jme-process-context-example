package ch.admin.bit.jeap.jme.processcontext.perftest.load;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LoadGeneratorTest {

    private final LoadGenerator loadGenerator = new LoadGenerator();

    @Test
    void generate_rateLimited_invokesExactCount() {
        int count = 20;
        Duration duration = Duration.ofSeconds(1);
        AtomicInteger invocations = new AtomicInteger(0);

        loadGenerator.generateDistributedOverTime(count, duration, _ -> invocations.incrementAndGet());

        assertEquals(count, invocations.get());
    }

    @Test
    void generate_rateLimited_spreadsInvocationsOverTime() {
        int count = 20;

        long burstStart = System.nanoTime();
        loadGenerator.generateBurst(count, _ -> {
        });
        long burstElapsed = System.nanoTime() - burstStart;

        long rateLimitedStart = System.nanoTime();
        loadGenerator.generateDistributedOverTime(count, Duration.ofSeconds(1), _ -> {
        });
        long rateLimitedElapsed = System.nanoTime() - rateLimitedStart;

        assertTrue(rateLimitedElapsed > burstElapsed * 10,
                "Rate-limited should be significantly slower than burst, but was only %dx slower"
                        .formatted(rateLimitedElapsed / Math.max(burstElapsed, 1)));
    }

    @Test
    void generate_burstMode_invokesExactCount() {
        int count = 100;
        AtomicInteger invocations = new AtomicInteger(0);

        loadGenerator.generateBurst(count, _ -> invocations.incrementAndGet());

        assertEquals(count, invocations.get());
    }

    @Test
    @Timeout(60)
    void generate_burst_stopsOnInterrupt() throws Exception {
        int count = 10_000;
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch gate = new CountDownLatch(1);
        AtomicInteger invocations = new AtomicInteger(0);

        ExecutorService runner = Executors.newSingleThreadExecutor();
        try {
            Future<?> future = runner.submit(() ->
                    loadGenerator.generateBurst(count, i -> {
                        invocations.incrementAndGet();
                        if (i == 0) {
                            started.countDown();
                            try {
                                gate.await();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException(e);
                            }
                        }
                    }));

            started.await();
            future.cancel(true);

            assertThrows(Exception.class, future::get);
            assertTrue(invocations.get() < count,
                    "Expected fewer than %d invocations but got %d".formatted(count, invocations.get()));
        } finally {
            runner.shutdownNow();
        }
    }

    @Test
    @Timeout(60)
    void generate_rateLimited_stopsOnInterrupt() throws Exception {
        int count = 10_000;
        Duration duration = Duration.ofMinutes(10);
        CountDownLatch started = new CountDownLatch(1);
        AtomicInteger invocations = new AtomicInteger(0);

        ExecutorService runner = Executors.newSingleThreadExecutor();
        try {
            Future<?> future = runner.submit(() ->
                    loadGenerator.generateDistributedOverTime(count, duration, _ -> {
                        started.countDown();
                        invocations.incrementAndGet();
                    }));

            started.await();
            future.cancel(true);

            assertThrows(Exception.class, future::get);
            assertTrue(invocations.get() < count,
                    "Expected fewer than %d invocations but got %d".formatted(count, invocations.get()));
        } finally {
            runner.shutdownNow();
        }
    }
}
