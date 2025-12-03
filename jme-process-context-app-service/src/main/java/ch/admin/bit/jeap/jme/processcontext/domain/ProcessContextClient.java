package ch.admin.bit.jeap.jme.processcontext.domain;

import ch.admin.bit.jeap.processcontext.archive.processsnapshot.v2.ProcessSnapshot;

import java.util.Optional;
import java.util.Set;

public interface ProcessContextClient {

    void createProcess(String id, String templateName, Set<ExternalReferenceDTO> externalReferences);

    Optional<ProcessSnapshot> getProcessSnapshot(String originProcessId, int version);

}

