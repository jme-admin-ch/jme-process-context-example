package ch.admin.bit.jeap.jme.processcontext.web.perftest;

import java.util.UUID;

public record TestRunDTO(UUID id, String reportPath, String statusPath) {
}
