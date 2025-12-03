package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;
import ch.admin.bit.jme.document.DocumentReference;
import ch.admin.bit.jme.document.JmeDocumentCreatedEvent;
import ch.admin.bit.jme.document.JmeDocumentCreatedEventPayload;
import ch.admin.bit.jme.document.JmeDocumentCreatedEventReferences;

public class JmeDocumentCreatedEventBuilder extends AbstractTestEventBuilder<JmeDocumentCreatedEventBuilder, JmeDocumentCreatedEvent> {

    private static final String DOCUMENT_REF_TYPE = "document";

    private String documentId;
    private String title;
    private String author;

    private JmeDocumentCreatedEventBuilder(String originProcessId) {
        super(JmeDocumentCreatedEvent::new, JmeDocumentCreatedEventReferences::new, originProcessId);
    }

    public static JmeDocumentCreatedEventBuilder createForProcessId(String originProcessId) {
        return new JmeDocumentCreatedEventBuilder(originProcessId);
    }

    public JmeDocumentCreatedEventBuilder documentId(String documentId) {
        this.documentId = documentId;
        return this;
    }

    public JmeDocumentCreatedEventBuilder title(String title) {
        this.title = title;
        return this;
    }

    public JmeDocumentCreatedEventBuilder author(String author) {
        this.author = author;
        return this;
    }

    @Override
    public JmeDocumentCreatedEvent build() {
        if (documentId == null) {
            throw AvroMessageBuilderException.propertyNull("documentId");
        }
        if (title == null) {
            throw AvroMessageBuilderException.propertyNull("title");
        }
        if (author == null) {
            throw AvroMessageBuilderException.propertyNull("author");
        }

        JmeDocumentCreatedEvent event = super.build();
        JmeDocumentCreatedEventPayload payload = JmeDocumentCreatedEventPayload.newBuilder()
                .setTitle(title)
                .setAuthor(author)
                .build();
        event.setPayload(payload);
        DocumentReference documentReference = DocumentReference.newBuilder()
                .setType(DOCUMENT_REF_TYPE)
                .setId(documentId)
                .build();
        JmeDocumentCreatedEventReferences references = JmeDocumentCreatedEventReferences.newBuilder()
                .setDocumentReference(documentReference)
                .build();
        event.setReferences(references);
        return event;
    }
}
