package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;
import ch.admin.bit.jme.document.JmeDocumentVersionCreatedEvent;
import ch.admin.bit.jme.document.JmeDocumentVersionCreatedEventPayload;
import ch.admin.bit.jme.document.JmeDocumentVersionCreatedEventReferences;
import ch.admin.bit.jme.document.VersionReference;

public class JmeDocumentVersionCreatedEventBuilder extends AbstractTestEventBuilder<JmeDocumentVersionCreatedEventBuilder, JmeDocumentVersionCreatedEvent> {

    private static final String DOCUMENT_REF_TYPE = "document";

    private String versionId;
    private String versionNumber;
    private String changes;

    private JmeDocumentVersionCreatedEventBuilder(String originProcessId) {
        super(JmeDocumentVersionCreatedEvent::new, JmeDocumentVersionCreatedEventReferences::new, originProcessId);
    }

    public static JmeDocumentVersionCreatedEventBuilder createForProcessId(String originProcessId) {
        return new JmeDocumentVersionCreatedEventBuilder(originProcessId);
    }

    public JmeDocumentVersionCreatedEventBuilder versionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    public JmeDocumentVersionCreatedEventBuilder versionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
        return this;
    }

    public JmeDocumentVersionCreatedEventBuilder changes(String title) {
        this.changes = title;
        return this;
    }

    @Override
    public JmeDocumentVersionCreatedEvent build() {
        if (versionId == null) {
            throw AvroMessageBuilderException.propertyNull("versionId");
        }
        if (versionNumber == null) {
            throw AvroMessageBuilderException.propertyNull("versionNumber");
        }
        if (changes == null) {
            throw AvroMessageBuilderException.propertyNull("changes");
        }

        JmeDocumentVersionCreatedEvent event = super.build();
        JmeDocumentVersionCreatedEventPayload payload = JmeDocumentVersionCreatedEventPayload.newBuilder()
                .setChanges(changes)
                .build();
        event.setPayload(payload);
        VersionReference versionReference = VersionReference.newBuilder()
                .setType(DOCUMENT_REF_TYPE)
                .setId(versionId)
                .setVersionNumber(versionNumber)
                .build();
        JmeDocumentVersionCreatedEventReferences references = JmeDocumentVersionCreatedEventReferences.newBuilder()
                .setVersionReference(versionReference)
                .build();
        event.setReferences(references);
        return event;
    }
}
