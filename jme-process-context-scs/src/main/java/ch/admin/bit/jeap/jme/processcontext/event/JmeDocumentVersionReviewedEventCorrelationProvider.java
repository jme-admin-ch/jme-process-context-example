package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageCorrelationProvider;
import ch.admin.bit.jme.document.JmeDocumentVersionReviewedEvent;

import java.util.Set;

public class JmeDocumentVersionReviewedEventCorrelationProvider implements MessageCorrelationProvider<JmeDocumentVersionReviewedEvent> {

    @Override
    public Set<String> getRelatedOriginTaskIds(JmeDocumentVersionReviewedEvent event) {
        return Set.of(event.getReferences().getVersionReference().getId());
    }

}
