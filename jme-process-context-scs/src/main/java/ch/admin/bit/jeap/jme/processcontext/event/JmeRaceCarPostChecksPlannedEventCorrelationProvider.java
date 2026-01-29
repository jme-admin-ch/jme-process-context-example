package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.carpostchecks.planned.JmeRaceCarPostChecksPlannedEvent;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageCorrelationProvider;

import java.util.HashSet;
import java.util.Set;

public class JmeRaceCarPostChecksPlannedEventCorrelationProvider implements MessageCorrelationProvider<JmeRaceCarPostChecksPlannedEvent> {

    @Override
    public Set<String> getRelatedOriginTaskIds(JmeRaceCarPostChecksPlannedEvent event) {
        return new HashSet<>(event.getPayload().getPostChecks());
    }

}
