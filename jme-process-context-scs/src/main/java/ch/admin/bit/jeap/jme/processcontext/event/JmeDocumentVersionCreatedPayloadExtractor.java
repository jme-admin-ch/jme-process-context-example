package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.processcontext.plugin.api.event.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.event.PayloadExtractor;
import ch.admin.bit.jme.document.JmeDocumentVersionCreatedEventPayload;

import java.util.Set;

public class JmeDocumentVersionCreatedPayloadExtractor implements PayloadExtractor<JmeDocumentVersionCreatedEventPayload> {

    @Override
    public Set<MessageData> getMessageData(JmeDocumentVersionCreatedEventPayload payload) {
        return Set.of(MessageData.builder()
                .key("changes")
                .value(payload.getChanges())
                .build()
        );
    }

}
