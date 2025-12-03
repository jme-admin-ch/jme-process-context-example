package ch.admin.bit.jeap.jme.processcontext.domain;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class ExternalReferenceDTO {
    private String name;
    private String value;
}
