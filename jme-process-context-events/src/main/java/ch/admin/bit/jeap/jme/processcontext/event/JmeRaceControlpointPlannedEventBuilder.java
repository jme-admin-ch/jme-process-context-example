package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.controlpoint.planned.JmeRaceControlpointPlannedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.controlpoint.planned.JmeRaceControlpointPlannedEventPayload;
import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;

import java.util.List;

public class JmeRaceControlpointPlannedEventBuilder extends AbstractTestEventBuilder<JmeRaceControlpointPlannedEventBuilder, JmeRaceControlpointPlannedEvent> {

    private List<String> controlPoints;

    private JmeRaceControlpointPlannedEventBuilder(String originProcessId) {
        super(JmeRaceControlpointPlannedEvent::new, originProcessId);
    }

    public static JmeRaceControlpointPlannedEventBuilder createForProcessId(String originProcessId) {
        return new JmeRaceControlpointPlannedEventBuilder(originProcessId);
    }

    public JmeRaceControlpointPlannedEventBuilder controlPoints(List<String> controlPoints) {
        this.controlPoints = controlPoints;
        return this;
    }

    @Override
    public JmeRaceControlpointPlannedEvent build() {
        if (controlPoints == null || controlPoints.isEmpty()) {
            throw AvroMessageBuilderException.propertyNull("controlPoint");
        }

        JmeRaceControlpointPlannedEvent event = super.build();
        JmeRaceControlpointPlannedEventPayload payload = JmeRaceControlpointPlannedEventPayload.newBuilder()
                .setControlPoints(this.controlPoints)
                .build();
        event.setPayload(payload);
        return event;
    }
}
