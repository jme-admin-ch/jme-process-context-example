package ch.admin.bit.jeap.jme.processcontext.web;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class NewDocumentVersionDTO {

    @NotNull
    private String versionId;
    @NotNull
    private String versionNumber;
    @NotNull
    private String changes;

}
