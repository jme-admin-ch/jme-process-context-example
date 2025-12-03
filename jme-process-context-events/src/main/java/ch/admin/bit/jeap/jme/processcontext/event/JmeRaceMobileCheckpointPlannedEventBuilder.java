package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.mobilecheckpoint.planned.JmeRaceMobileCheckpointPlannedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.mobilecheckpoint.planned.JmeRaceMobileCheckpointPlannedEventPayload;
import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;

import java.util.List;

public class JmeRaceMobileCheckpointPlannedEventBuilder extends AbstractTestEventBuilder<JmeRaceMobileCheckpointPlannedEventBuilder, JmeRaceMobileCheckpointPlannedEvent> {

    private List<String> checkpoints;

    private JmeRaceMobileCheckpointPlannedEventBuilder(String originProcessId) {
        super(JmeRaceMobileCheckpointPlannedEvent::new, originProcessId);
    }

    public static JmeRaceMobileCheckpointPlannedEventBuilder createForProcessId(String originProcessId) {
        return new JmeRaceMobileCheckpointPlannedEventBuilder(originProcessId);
    }

    public JmeRaceMobileCheckpointPlannedEventBuilder checkpoints(List<String> checkpoints) {
        this.checkpoints = checkpoints;
        return this;
    }

    @Override
    public JmeRaceMobileCheckpointPlannedEvent build() {
        if (checkpoints == null || checkpoints.isEmpty()) {
            throw AvroMessageBuilderException.propertyNull("checkpoints");
        }

        JmeRaceMobileCheckpointPlannedEvent event = super.build();
        JmeRaceMobileCheckpointPlannedEventPayload payload = JmeRaceMobileCheckpointPlannedEventPayload.newBuilder()
                .setCheckpoints(this.checkpoints)
                .build();
        event.setPayload(payload);
        return event;
    }
}
