package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.processcontext.plugin.api.event.MessageCorrelationProvider;
import ch.admin.bit.jme.document.JmeDocumentVersionCreatedEvent;

import java.util.Set;

public class JmeDocumentVersionCreatedEventCorrelationProvider implements MessageCorrelationProvider<JmeDocumentVersionCreatedEvent> {

    @Override
    public Set<String> getRelatedOriginTaskIds(JmeDocumentVersionCreatedEvent event) {
        return Set.of(event.getReferences().getVersionReference().getId());
    }

}
