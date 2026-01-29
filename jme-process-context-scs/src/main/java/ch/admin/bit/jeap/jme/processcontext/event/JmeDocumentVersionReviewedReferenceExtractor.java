package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.message.ReferenceExtractor;
import ch.admin.bit.jme.document.JmeDocumentVersionReviewedEventReferences;

import java.util.Collections;
import java.util.Set;

public class JmeDocumentVersionReviewedReferenceExtractor implements ReferenceExtractor<JmeDocumentVersionReviewedEventReferences> {

    @Override
    public Set<MessageData> getMessageData(JmeDocumentVersionReviewedEventReferences references) {
        String reviewId = references.getReviewReference().getId();
        String versionNumber = references.getVersionReference().getVersionNumber();
        MessageData messageData = MessageData.builder()
                .key("reviewId")
                .value(reviewId)
                .role(versionNumber)
                .build();
        return Collections.singleton(messageData);
    }

}
