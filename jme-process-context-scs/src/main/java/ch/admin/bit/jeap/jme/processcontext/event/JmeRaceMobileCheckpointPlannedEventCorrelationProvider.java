package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.mobilecheckpoint.planned.JmeRaceMobileCheckpointPlannedEvent;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageCorrelationProvider;

import java.util.HashSet;
import java.util.Set;

public class JmeRaceMobileCheckpointPlannedEventCorrelationProvider implements MessageCorrelationProvider<JmeRaceMobileCheckpointPlannedEvent> {

    @Override
    public Set<String> getRelatedOriginTaskIds(JmeRaceMobileCheckpointPlannedEvent event) {
        return new HashSet<>(event.getPayload().getCheckpoints());
    }

}
