package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.tirepressure.checked.JmeRaceCarTirePressureCheckedEvent;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageCorrelationProvider;

import java.util.Set;

public class JmeRaceCarTirePressureCheckedEventCorrelationProvider implements MessageCorrelationProvider<JmeRaceCarTirePressureCheckedEvent> {

    @Override
    public Set<String> getRelatedOriginTaskIds(JmeRaceCarTirePressureCheckedEvent event) {
        String maintenanceId = event.getReferences().getMaintenanceReference().getMaintenanceId();
        return Set.of(maintenanceId + "_" + event.getReferences().getTireReference().getTireId());
    }

    @Override
    public Set<String> getOriginProcessIds(JmeRaceCarTirePressureCheckedEvent event) {
        return Set.of(event.getProcessId());
    }
}
