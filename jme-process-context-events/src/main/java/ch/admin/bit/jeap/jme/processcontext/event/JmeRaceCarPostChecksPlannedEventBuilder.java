package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.carpostchecks.planned.JmeRaceCarPostChecksPlannedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.carpostchecks.planned.JmeRaceCarPostChecksPlannedEventPayload;
import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;

import java.util.List;

public class JmeRaceCarPostChecksPlannedEventBuilder extends AbstractTestEventBuilder<JmeRaceCarPostChecksPlannedEventBuilder, JmeRaceCarPostChecksPlannedEvent> {

    private List<String> postChecks;

    private JmeRaceCarPostChecksPlannedEventBuilder(String originProcessId) {
        super(JmeRaceCarPostChecksPlannedEvent::new, originProcessId);
    }

    public static JmeRaceCarPostChecksPlannedEventBuilder createForProcessId(String originProcessId) {
        return new JmeRaceCarPostChecksPlannedEventBuilder(originProcessId);
    }

    public JmeRaceCarPostChecksPlannedEventBuilder postChecks(List<String> postChecks) {
        this.postChecks = postChecks;
        return this;
    }

    @Override
    public JmeRaceCarPostChecksPlannedEvent build() {
        if (postChecks == null) {
            throw AvroMessageBuilderException.propertyNull("postChecks");
        }

        JmeRaceCarPostChecksPlannedEvent event = super.build();
        JmeRaceCarPostChecksPlannedEventPayload payload = JmeRaceCarPostChecksPlannedEventPayload.newBuilder()
                .setPostChecks(this.postChecks)
                .build();
        event.setPayload(payload);
        return event;
    }
}
