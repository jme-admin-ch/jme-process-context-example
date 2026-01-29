package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.domainevent.DomainEvent;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageCorrelationProvider;

import java.util.Set;

public class JmeSingleInstanceTaskCorrelationProvider implements MessageCorrelationProvider<DomainEvent> {

    @Override
    public Set<String> getOriginProcessIds(DomainEvent event) {
        return Set.of(event.getOptionalProcessId().orElseThrow());
    }
}
