package ch.admin.bit.jeap.jme.processcontext.web;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class DocumentReviewDTO {
    @NotNull
    private String documentId;
    @NotNull
    private String status;

}
