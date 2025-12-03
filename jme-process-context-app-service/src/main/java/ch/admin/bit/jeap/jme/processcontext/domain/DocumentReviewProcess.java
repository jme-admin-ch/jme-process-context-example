package ch.admin.bit.jeap.jme.processcontext.domain;

import ch.admin.bit.jeap.jme.processcontext.web.DocumentReviewDTO;
import ch.admin.bit.jeap.jme.processcontext.web.NewDocumentReviewProcessDTO;
import ch.admin.bit.jeap.jme.processcontext.web.NewDocumentVersionDTO;
import ch.admin.bit.jeap.jme.processcontext.web.NewDocumentVersionReviewDTO;
import lombok.Getter;

@Getter
public class DocumentReviewProcess implements Process {

    private final MessagePublisher messagePublisher;
    private final String id;

    public static DocumentReviewProcess create(MessagePublisher messagePublisher, String processId, NewDocumentReviewProcessDTO newDocumentReviewProcessDTO) {
        DocumentReviewProcess testProcess = new DocumentReviewProcess(messagePublisher, processId);
        messagePublisher.documentCreated(processId, newDocumentReviewProcessDTO.getDocumentId(), newDocumentReviewProcessDTO.getTitle(), newDocumentReviewProcessDTO.getAuthor());

        return testProcess;
    }

    private DocumentReviewProcess(MessagePublisher messagePublisher, String processId) {
        this.messagePublisher = messagePublisher;
        this.id = processId;
    }

    public static void reviewDocument(MessagePublisher messagePublisher, String processId, DocumentReviewDTO documentReviewDTO) {
        messagePublisher.documentReviewCreated(processId, documentReviewDTO.getDocumentId(), documentReviewDTO.getStatus());
    }

    public static void createDocumentVersion(MessagePublisher messagePublisher, String processId, NewDocumentVersionDTO newDocumentVersionDTO) {
        messagePublisher.documentVersionCreated(processId, newDocumentVersionDTO.getVersionId(), newDocumentVersionDTO.getVersionNumber(), newDocumentVersionDTO.getChanges());
    }

    public static void reviewDocumentVersion(MessagePublisher messagePublisher, String processId, NewDocumentVersionReviewDTO newDocumentVersionReviewDTO) {
        messagePublisher.documentVersionReviewed(processId, newDocumentVersionReviewDTO.getReviewId(), newDocumentVersionReviewDTO.getVersionId(), newDocumentVersionReviewDTO.getVersionNumber(), newDocumentVersionReviewDTO.getStatus(), newDocumentVersionReviewDTO.getComments());
    }
}
