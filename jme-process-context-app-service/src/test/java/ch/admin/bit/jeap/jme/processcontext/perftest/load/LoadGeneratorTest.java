package ch.admin.bit.jeap.jme.processcontext.perftest.load;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
