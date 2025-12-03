package ch.admin.bit.jeap.jme.processcontext.restclient;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class ExternalReferenceDTO {
    String name;
    String value;
}
