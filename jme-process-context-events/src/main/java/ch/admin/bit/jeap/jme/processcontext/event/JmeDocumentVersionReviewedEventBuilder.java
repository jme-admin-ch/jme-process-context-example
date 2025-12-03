package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;
import ch.admin.bit.jme.document.*;

public class JmeDocumentVersionReviewedEventBuilder extends AbstractTestEventBuilder<JmeDocumentVersionReviewedEventBuilder, JmeDocumentVersionReviewedEvent> {

    private static final String VERSION_REF_TYPE = "version";
    private static final String REVIEW_REF_TYPE = "review";

    private String reviewId;
    private String versionId;
    private String versionNumber;
    private String status;
    private String comments;

    private JmeDocumentVersionReviewedEventBuilder(String originProcessId) {
        super(JmeDocumentVersionReviewedEvent::new, JmeDocumentVersionReviewedEventReferences::new, originProcessId);
    }

    public static JmeDocumentVersionReviewedEventBuilder createForProcessId(String originProcessId) {
        return new JmeDocumentVersionReviewedEventBuilder(originProcessId);
    }

    public JmeDocumentVersionReviewedEventBuilder reviewId(String reviewId) {
        this.reviewId = reviewId;
        return this;
    }

    public JmeDocumentVersionReviewedEventBuilder versionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    public JmeDocumentVersionReviewedEventBuilder versionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
        return this;
    }

    public JmeDocumentVersionReviewedEventBuilder status(String status) {
        this.status = status;
        return this;
    }

    public JmeDocumentVersionReviewedEventBuilder comments(String comments) {
        this.comments = comments;
        return this;
    }

    @Override
    public JmeDocumentVersionReviewedEvent build() {
        if (reviewId == null) {
            throw AvroMessageBuilderException.propertyNull("reviewId");
        }
        if (versionId == null) {
            throw AvroMessageBuilderException.propertyNull("versionId");
        }
        if (versionNumber == null) {
            throw AvroMessageBuilderException.propertyNull("versionNumber");
        }
        if (status == null) {
            throw AvroMessageBuilderException.propertyNull("status");
        }

        JmeDocumentVersionReviewedEvent event = super.build();
        JmeDocumentVersionReviewedEventPayload payload = JmeDocumentVersionReviewedEventPayload.newBuilder()
                .setStatus(status)
                .setComments(comments)
                .build();
        event.setPayload(payload);
        VersionReference versionReference = VersionReference.newBuilder()
                .setType(VERSION_REF_TYPE)
                .setId(versionId)
                .setVersionNumber(versionNumber)
                .build();
        JmeReviewReference reviewReference = JmeReviewReference.newBuilder()
                .setType(REVIEW_REF_TYPE)
                .setId(reviewId)
                .build();
        JmeDocumentVersionReviewedEventReferences references = JmeDocumentVersionReviewedEventReferences.newBuilder()
                .setVersionReference(versionReference)
                .setReviewReference(reviewReference)
                .build();
        event.setReferences(references);
        return event;
    }
}
