package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.carpostchecks.completed.JmeRaceCarPostChecksCompletedEvent;
import ch.admin.bit.jeap.processcontext.plugin.api.event.DomainEventCorrelationProvider;

import java.util.Set;

public class JmeRaceCarPostChecksCompletedEventCorrelationProvider  implements DomainEventCorrelationProvider<JmeRaceCarPostChecksCompletedEvent> {

    @Override
    public Set<String> getRelatedOriginTaskIds(JmeRaceCarPostChecksCompletedEvent event) {
        return Set.of("postchecks");
    }
}
