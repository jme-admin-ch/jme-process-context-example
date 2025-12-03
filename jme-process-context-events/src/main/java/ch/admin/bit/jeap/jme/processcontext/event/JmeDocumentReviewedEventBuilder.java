package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;
import ch.admin.bit.jme.document.DocumentReference;
import ch.admin.bit.jme.document.JmeDocumentReviewedEvent;
import ch.admin.bit.jme.document.JmeDocumentReviewedEventPayload;
import ch.admin.bit.jme.document.JmeDocumentReviewedEventReferences;

public class JmeDocumentReviewedEventBuilder extends AbstractTestEventBuilder<JmeDocumentReviewedEventBuilder, JmeDocumentReviewedEvent> {

    private static final String DOCUMENT_REF_TYPE = "document";

    private String documentId;
    private String status;

    private JmeDocumentReviewedEventBuilder(String originProcessId) {
        super(JmeDocumentReviewedEvent::new, JmeDocumentReviewedEventReferences::new, originProcessId);
    }

    public static JmeDocumentReviewedEventBuilder createForProcessId(String originProcessId) {
        return new JmeDocumentReviewedEventBuilder(originProcessId);
    }

    public JmeDocumentReviewedEventBuilder documentId(String documentId) {
        this.documentId = documentId;
        return this;
    }

    public JmeDocumentReviewedEventBuilder status(String status) {
        this.status = status;
        return this;
    }

    @Override
    public JmeDocumentReviewedEvent build() {
        if (documentId == null) {
            throw AvroMessageBuilderException.propertyNull("documentId");
        }
        if (status == null) {
            throw AvroMessageBuilderException.propertyNull("status");
        }

        JmeDocumentReviewedEvent event = super.build();
        JmeDocumentReviewedEventPayload payload = JmeDocumentReviewedEventPayload.newBuilder()
                .setStatus(status)
                .build();
        event.setPayload(payload);
        DocumentReference documentReference = DocumentReference.newBuilder()
                .setType(DOCUMENT_REF_TYPE)
                .setId(documentId)
                .build();
        JmeDocumentReviewedEventReferences references = JmeDocumentReviewedEventReferences.newBuilder()
                .setDocumentReference(documentReference)
                .build();
        event.setReferences(references);
        return event;
    }
}
