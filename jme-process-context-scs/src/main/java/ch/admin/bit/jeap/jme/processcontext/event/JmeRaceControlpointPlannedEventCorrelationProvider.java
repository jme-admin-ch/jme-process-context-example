package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.controlpoint.planned.JmeRaceControlpointPlannedEvent;
import ch.admin.bit.jeap.processcontext.plugin.api.event.DomainEventCorrelationProvider;

import java.util.HashSet;
import java.util.Set;

public class JmeRaceControlpointPlannedEventCorrelationProvider implements DomainEventCorrelationProvider<JmeRaceControlpointPlannedEvent> {

    @Override
    public Set<String> getRelatedOriginTaskIds(JmeRaceControlpointPlannedEvent event) {
        return new HashSet<>(event.getPayload().getControlPoints());
    }

}
