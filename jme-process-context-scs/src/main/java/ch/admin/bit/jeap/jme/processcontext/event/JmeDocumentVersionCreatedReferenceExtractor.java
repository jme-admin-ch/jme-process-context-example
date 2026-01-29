package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.message.ReferenceExtractor;
import ch.admin.bit.jme.document.JmeDocumentVersionCreatedEventReferences;

import java.util.Collections;
import java.util.Set;

public class JmeDocumentVersionCreatedReferenceExtractor implements ReferenceExtractor<JmeDocumentVersionCreatedEventReferences> {

    @Override
    public Set<MessageData> getMessageData(JmeDocumentVersionCreatedEventReferences references) {
        String versionId = references.getVersionReference().getId();
        String versionNumber = references.getVersionReference().getVersionNumber();
        MessageData messageData = MessageData.builder()
                .key("versionId")
                .value(versionId)
                .role(versionNumber)
                .build();
        return Collections.singleton(messageData);
    }

}
