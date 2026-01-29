package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.required.JmeRaceCarMaintenanceRequiredEvent;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageCorrelationProvider;

import java.util.Set;
import java.util.stream.Collectors;

public class JmeRaceCarMaintenanceRequiredEventCorrelationProvider implements MessageCorrelationProvider<JmeRaceCarMaintenanceRequiredEvent> {

    @Override
    public Set<String> getRelatedOriginTaskIds(JmeRaceCarMaintenanceRequiredEvent event) {
        String maintenanceId = event.getReferences().getMaintenanceReference().getMaintenanceId();
        Set<String> tireIds = Set.of("front-left", "front-right", "rear-left", "rear-right");
        return tireIds.stream()
                .map(tireId -> maintenanceId + "_" + tireId)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getOriginProcessIds(JmeRaceCarMaintenanceRequiredEvent event) {
        return Set.of(event.getProcessId());
    }
}
