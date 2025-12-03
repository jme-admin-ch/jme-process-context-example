package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.carpostchecks.completed.JmeRaceCarPostChecksCompletedEventPayload;
import ch.admin.bit.jeap.processcontext.plugin.api.event.EventData;
import ch.admin.bit.jeap.processcontext.plugin.api.event.PayloadExtractor;

import java.util.Set;
import java.util.stream.Collectors;

public class JmeRaceCarPostChecksCompletedEventPayloadExtractor implements PayloadExtractor<JmeRaceCarPostChecksCompletedEventPayload> {

    @Override
    public Set<EventData> getEventData(JmeRaceCarPostChecksCompletedEventPayload payload) {

        return payload.getResults().stream().map(r -> new EventData("race-car-number", r.getCarNumber())).collect(Collectors.toSet());

    }
}
