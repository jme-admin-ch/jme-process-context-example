package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.carpostchecks.completed.JmeRaceCarPostCheckResult;
import ch.admin.bit.jeap.jme.processcontext.event.race.carpostchecks.completed.JmeRaceCarPostChecksCompletedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.carpostchecks.completed.JmeRaceCarPostChecksCompletedEventPayload;
import ch.admin.bit.jeap.jme.processcontext.event.race.carpostchecks.completed.JmeRaceCarPostChecksCompletedEventReferences;
import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;

import java.util.List;

public class JmeRaceCarPostChecksCompletedEventBuilder extends AbstractTestEventBuilder<JmeRaceCarPostChecksCompletedEventBuilder, JmeRaceCarPostChecksCompletedEvent> {

    private String raceId;
    private List<JmeRaceCarPostCheckResult> results;

    private JmeRaceCarPostChecksCompletedEventBuilder() {
        super(JmeRaceCarPostChecksCompletedEvent::new, JmeRaceCarPostChecksCompletedEventReferences::new, null);
    }

    public static JmeRaceCarPostChecksCompletedEventBuilder create() {
        return new JmeRaceCarPostChecksCompletedEventBuilder();
    }

    @Override
    protected final String getEventVersion() {
        return "1.0.0";
    }


    public JmeRaceCarPostChecksCompletedEventBuilder raceId(String raceId) {
        this.raceId = raceId;
        return this;
    }

    public JmeRaceCarPostChecksCompletedEventBuilder results(List<JmeRaceCarPostCheckResult> results) {
        this.results = results;
        return this;
    }

    @Override
    public JmeRaceCarPostChecksCompletedEvent build() {
        if (results == null) {
            throw AvroMessageBuilderException.propertyNull("results");
        }

        JmeRaceCarPostChecksCompletedEvent event = super.build();
        event.setPayload(JmeRaceCarPostChecksCompletedEventPayload.newBuilder()
                .setRaceId(raceId)
                .setResults(results).build());
        return event;
    }
}
