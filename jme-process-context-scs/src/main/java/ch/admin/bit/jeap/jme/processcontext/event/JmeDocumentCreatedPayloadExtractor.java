package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.processcontext.plugin.api.event.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.event.PayloadExtractor;
import ch.admin.bit.jme.document.JmeDocumentCreatedEventPayload;

import java.util.Set;

public class JmeDocumentCreatedPayloadExtractor implements PayloadExtractor<JmeDocumentCreatedEventPayload> {

    @Override
    public Set<MessageData> getMessageData(JmeDocumentCreatedEventPayload payload) {
        return Set.of(
                MessageData.builder()
                    .key("author")
                    .value(payload.getAuthor())
                    .build(),
                MessageData.builder()
                    .key("title")
                    .value(payload.getTitle())
                    .build()
        );
    }

}
