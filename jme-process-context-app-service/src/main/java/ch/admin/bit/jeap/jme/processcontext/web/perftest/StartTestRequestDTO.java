package ch.admin.bit.jeap.jme.processcontext.web.perftest;

import ch.admin.bit.jeap.jme.processcontext.perftest.TestScenarioType;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.util.Map;

public record StartTestRequestDTO(@NotNull TestScenarioType scenario, Map<String, Object> parameters) {
    @Override
    public Map<String, Object> parameters() {
        return parameters == null ? Map.of() : parameters;
    }

    Duration timeout() {
        String timeoutMinutes = parameters.getOrDefault("timeoutMinutes", "10").toString();
        return Duration.ofMinutes(Integer.parseInt(timeoutMinutes));
    }
}
