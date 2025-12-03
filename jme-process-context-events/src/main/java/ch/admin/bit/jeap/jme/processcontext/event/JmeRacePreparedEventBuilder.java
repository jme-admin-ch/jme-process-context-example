package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.prepared.JmeRacePreparedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.prepared.JmeRacePreparedEventPayload;
import ch.admin.bit.jeap.jme.processcontext.event.race.prepared.JmeRacePreparedEventReferences;
import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;

public class JmeRacePreparedEventBuilder extends AbstractTestEventBuilder<JmeRacePreparedEventBuilder, JmeRacePreparedEvent> {

    private String raceId;
    private String raceCarNumber;

    private JmeRacePreparedEventBuilder(String originProcessId) {
        super(JmeRacePreparedEvent::new, JmeRacePreparedEventReferences::new, originProcessId);
    }

    public static JmeRacePreparedEventBuilder createForProcessId(String originProcessId) {
        return new JmeRacePreparedEventBuilder(originProcessId);
    }

    @Override
    protected final String getEventVersion() {
        return "1.1.0";
    }

    public JmeRacePreparedEventBuilder raceId(String raceId) {
        this.raceId = raceId;
        return this;
    }

    public JmeRacePreparedEventBuilder raceCarNumber(String raceCarNumber) {
        this.raceCarNumber = raceCarNumber;
        return this;
    }

    @Override
    public JmeRacePreparedEvent build() {
        if (raceId == null) {
            throw AvroMessageBuilderException.propertyNull("raceId");
        }
        if (raceCarNumber == null) {
            throw AvroMessageBuilderException.propertyNull("raceCarNumber");
        }
        JmeRacePreparedEvent event = super.build();
        JmeRacePreparedEventPayload payload = JmeRacePreparedEventPayload.newBuilder()
                .setRaceId(raceId)
                .setRaceCarNumber(raceCarNumber)
                .build();
        event.setPayload(payload);
        return event;
    }
}
