package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.message.PayloadExtractor;
import ch.admin.bit.jme.document.JmeDocumentReviewedEventPayload;

import java.util.Set;

public class JmeDocumentReviewedPayloadExtractor implements PayloadExtractor<JmeDocumentReviewedEventPayload> {

    @Override
    public Set<MessageData> getMessageData(JmeDocumentReviewedEventPayload payload) {
        return Set.of(MessageData.builder()
                        .key("status")
                        .value(payload.getStatus())
                        .build());
    }

}
