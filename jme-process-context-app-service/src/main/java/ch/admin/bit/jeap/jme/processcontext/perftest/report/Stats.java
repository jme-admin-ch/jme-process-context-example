package ch.admin.bit.jeap.jme.processcontext.perftest.report;

import lombok.Builder;

import java.time.Duration;

@Builder
public record Stats(Duration avgDuration,
                    Duration medianDuration,
                    Duration minDuration,
                    Duration maxDuration) {
    public String formatted() {
        return String.format("""
                        Min duration: %sms
                        Max duration: %sms
                        Avg duration: %sms
                        Median duration: %sms""",
                minDuration.toMillis(), maxDuration.toMillis(), avgDuration.toMillis(), medianDuration.toMillis());
    }
}
