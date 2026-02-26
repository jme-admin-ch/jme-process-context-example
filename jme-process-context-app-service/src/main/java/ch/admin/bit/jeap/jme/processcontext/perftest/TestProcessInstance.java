package ch.admin.bit.jeap.jme.processcontext.perftest;

import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class TestProcessInstance {

    private final String originProcessId;
    private final LocalDateTime firstEventCreatedAt;
    private LocalDateTime processCreatedAt;
    private LocalDateTime processCompletedAt;
    private Duration completionDuration;
    private Duration creationDelay;
    private String state;

    public TestProcessInstance(String originProcessId, LocalDateTime firstEventCreatedAt) {
        this.originProcessId = originProcessId;
        this.firstEventCreatedAt = firstEventCreatedAt;
    }

    public void createdAt(LocalDateTime processCreatedAt) {
        this.creationDelay = Duration.between(firstEventCreatedAt, processCreatedAt);
        this.processCreatedAt = processCreatedAt;
    }

    public void completed(LocalDateTime processCompletedAt) {
        this.processCompletedAt = processCompletedAt;
        this.completionDuration = Duration.between(processCreatedAt, processCompletedAt);
    }

    public void state(String state) {
        this.state = state;
    }

    public boolean isCompleted() {
        return processCompletedAt != null;
    }
}
