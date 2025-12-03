package ch.admin.bit.jeap.jme.processcontext.event;


import ch.admin.bit.jeap.jme.processcontext.event.race.controlpoint.passed.JmeRaceControlpointPassedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.controlpoint.passed.JmeRaceControlpointPassedEventReferences;
import ch.admin.bit.jeap.jme.processcontext.event.race.controlpoint.passed.JmeRaceControlpointReference;
import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;

public class JmeRaceControlpointPassedEventBuilder extends AbstractTestEventBuilder<JmeRaceControlpointPassedEventBuilder, JmeRaceControlpointPassedEvent> {

    private static final String CONTROL_POINT_REF_TYPE = "controlPoint";

    private String controlPoint;

    private JmeRaceControlpointPassedEventBuilder(String originProcessId) {
        super(JmeRaceControlpointPassedEvent::new, JmeRaceControlpointPassedEventReferences::new, originProcessId);
    }

    public static JmeRaceControlpointPassedEventBuilder createForProcessId(String originProcessId) {
        return new JmeRaceControlpointPassedEventBuilder(originProcessId);
    }

    public JmeRaceControlpointPassedEventBuilder controlPoint(String controlPoint) {
        this.controlPoint = controlPoint;
        return this;
    }

    @Override
    protected final String getEventVersion() {
        return "1.1.0";
    }

    @Override
    public JmeRaceControlpointPassedEvent build() {
        if (controlPoint == null) {
            throw AvroMessageBuilderException.propertyNull("controlPoint");
        }

        JmeRaceControlpointPassedEvent event = super.build();
        JmeRaceControlpointPassedEventReferences references = JmeRaceControlpointPassedEventReferences.newBuilder()
                .setControlPointBuilder(JmeRaceControlpointReference.newBuilder()
                        .setType(CONTROL_POINT_REF_TYPE)
                        .setControlPoint(controlPoint))
                .build();
        event.setReferences(references);
        return event;
    }
}
