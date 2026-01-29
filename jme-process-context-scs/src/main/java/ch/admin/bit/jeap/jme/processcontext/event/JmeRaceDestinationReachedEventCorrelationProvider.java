package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.destination.reached.JmeRaceDestinationReachedEvent;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageCorrelationProvider;

import java.util.Set;

import static java.util.Collections.emptySet;

public class JmeRaceDestinationReachedEventCorrelationProvider implements MessageCorrelationProvider<JmeRaceDestinationReachedEvent> {

    @Override
    public Set<String> getOriginProcessIds(JmeRaceDestinationReachedEvent message) {
        return message.getOptionalProcessId()
                .map(s -> s+"-dopingProcess")
                .map(Set::of)
                .orElse(emptySet());
    }

}
