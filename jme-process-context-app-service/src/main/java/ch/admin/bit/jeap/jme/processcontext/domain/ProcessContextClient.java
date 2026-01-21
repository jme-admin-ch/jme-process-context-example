package ch.admin.bit.jeap.jme.processcontext.domain;

import ch.admin.bit.jeap.processcontext.archive.processsnapshot.v2.ProcessSnapshot;

import java.util.Optional;

public interface ProcessContextClient {

    Optional<ProcessSnapshot> getProcessSnapshot(String originProcessId, int version);

}

