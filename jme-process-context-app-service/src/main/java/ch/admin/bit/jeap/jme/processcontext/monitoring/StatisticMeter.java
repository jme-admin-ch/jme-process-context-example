package ch.admin.bit.jeap.jme.processcontext.monitoring;

import ch.admin.bit.jeap.jme.processcontext.db.StatisticService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Service meant primarily for load and performance tests
 */
@Slf4j
public class StatisticMeter implements MeterBinder {

    private static final String JEAP_PCS_SECONDS = "jeap_pcs_seconds";
    private static final String JEAP_PCS_INSTANCES = "jeap_pcs_instances";
    private static final String DURATION = "duration";

    private final StatisticService statisticService;

    public StatisticMeter(final StatisticService statisticService) {
        Objects.requireNonNull(statisticService, "statisticService cannot be null");
        this.statisticService = statisticService;
    }

    @Override
    public void bindTo(@NonNull final MeterRegistry meterRegistry) {
        Gauge.builder(JEAP_PCS_INSTANCES, this, value -> statisticService.countStarted().getCount() )
                .tag("state", "started")
                .baseUnit("count")
                .register(meterRegistry);
        Gauge.builder(JEAP_PCS_INSTANCES, this, value -> statisticService.countCompleted().getCount() )
                .tag("state", "completed")
                .baseUnit("count")
                .register(meterRegistry);

        Gauge.builder(JEAP_PCS_SECONDS, this, value -> statisticService.minDuration() )
                .tag("type", "min")
                .baseUnit(DURATION)
                .register(meterRegistry);

        Gauge.builder(JEAP_PCS_SECONDS, this, value -> statisticService.maxDuration() )
                .tag("type", "max")
                .baseUnit(DURATION)
                .register(meterRegistry);

        Gauge.builder(JEAP_PCS_SECONDS, this, value -> statisticService.avgDuration() )
                .tag("type", "avg")
                .baseUnit(DURATION)
                .register(meterRegistry);

        Gauge.builder(JEAP_PCS_SECONDS, this, value -> statisticService.totalDuration() )
                .tag("type", "total")
                .baseUnit(DURATION)
                .register(meterRegistry);
    }

}
