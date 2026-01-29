package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.controlpoint.passed.JmeRaceControlpointPassedEvent;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageCorrelationProvider;

import java.util.Set;

public class JmeRaceControlpointPassedEventCorrelationProvider implements MessageCorrelationProvider<JmeRaceControlpointPassedEvent> {

    @Override
    public Set<String> getRelatedOriginTaskIds(JmeRaceControlpointPassedEvent event) {
        return Set.of(event.getReferences().getControlPoint().getControlPoint());
    }
}
