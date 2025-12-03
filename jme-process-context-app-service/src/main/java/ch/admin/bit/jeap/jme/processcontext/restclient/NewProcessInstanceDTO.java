package ch.admin.bit.jeap.jme.processcontext.restclient;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
class NewProcessInstanceDTO {
    private String processTemplateName;
    private Set<ExternalReferenceDTO> externalReferences;
}
