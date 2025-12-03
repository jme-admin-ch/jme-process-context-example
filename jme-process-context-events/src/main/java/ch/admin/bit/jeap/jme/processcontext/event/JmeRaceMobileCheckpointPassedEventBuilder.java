package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.mobilecheckpoint.passed.JmeRaceMobileCheckpointPassedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.mobilecheckpoint.passed.JmeRaceMobileCheckpointPassedEventPayload;
import ch.admin.bit.jeap.jme.processcontext.event.race.mobilecheckpoint.passed.JmeRaceMobileCheckpointPassedEventReferences;
import ch.admin.bit.jeap.jme.processcontext.event.race.mobilecheckpoint.passed.JmeRaceMobileCheckpointReference;
import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class JmeRaceMobileCheckpointPassedEventBuilder extends AbstractTestEventBuilder<JmeRaceMobileCheckpointPassedEventBuilder, JmeRaceMobileCheckpointPassedEvent> {

    private static final String CHECKPOINT_REF_TYPE = "checkpoint";

    private String state;
    private String taskId;
    private LocalDateTime controlDate;
    private String checkpoint;

    private JmeRaceMobileCheckpointPassedEventBuilder(String originProcessId) {
        super(JmeRaceMobileCheckpointPassedEvent::new, JmeRaceMobileCheckpointPassedEventReferences::new, originProcessId);
    }

    public static JmeRaceMobileCheckpointPassedEventBuilder createForProcessId(String originProcessId) {
        return new JmeRaceMobileCheckpointPassedEventBuilder(originProcessId);
    }

    public JmeRaceMobileCheckpointPassedEventBuilder checkpoint(String checkpoint) {
        this.checkpoint = checkpoint;
        return this;
    }

    public JmeRaceMobileCheckpointPassedEventBuilder state(String state) {
        this.state = state;
        return this;
    }

    public JmeRaceMobileCheckpointPassedEventBuilder taskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public JmeRaceMobileCheckpointPassedEventBuilder controlDate(LocalDateTime controlDate) {
        this.controlDate = controlDate;
        return this;
    }


    @Override
    protected final String getEventVersion() {
        return "1.1.0";
    }

    @Override
    public JmeRaceMobileCheckpointPassedEvent build() {
        if (checkpoint == null) {
            throw AvroMessageBuilderException.propertyNull("checkpoint");
        }
        JmeRaceMobileCheckpointPassedEvent event = super.build();

        JmeRaceMobileCheckpointPassedEventReferences references = JmeRaceMobileCheckpointPassedEventReferences.newBuilder()
                .setCheckpointBuilder(JmeRaceMobileCheckpointReference.newBuilder()
                        .setType(CHECKPOINT_REF_TYPE)
                        .setCheckpoint(checkpoint))
                .build();
        event.setReferences(references);

        JmeRaceMobileCheckpointPassedEventPayload payload = JmeRaceMobileCheckpointPassedEventPayload.newBuilder()
                .setState(state)
                .setTaskId(taskId)
                .setControlDate(controlDate.toInstant(ZoneOffset.UTC))
                .build();
        event.setPayload(payload);
        return event;
    }
}
