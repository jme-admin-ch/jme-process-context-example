package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.message.ReferenceExtractor;
import ch.admin.bit.jme.document.JmeDocumentCreatedEventReferences;

import java.util.Collections;
import java.util.Set;

public class JmeDocumentCreatedReferenceExtractor implements ReferenceExtractor<JmeDocumentCreatedEventReferences> {

    @Override
    public Set<MessageData> getMessageData(JmeDocumentCreatedEventReferences references) {
        String documentId = references.getDocumentReference().getId();
        MessageData messageData = MessageData.builder()
                .key("documentId")
                .value(documentId)
                .build();
        return Collections.singleton(messageData);
    }

}
