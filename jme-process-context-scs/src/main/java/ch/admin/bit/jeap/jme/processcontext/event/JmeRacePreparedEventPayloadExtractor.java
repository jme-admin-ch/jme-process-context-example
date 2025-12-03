package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.prepared.JmeRacePreparedEventPayload;
import ch.admin.bit.jeap.processcontext.plugin.api.event.EventData;
import ch.admin.bit.jeap.processcontext.plugin.api.event.PayloadExtractor;

import java.util.Set;

@SuppressWarnings("unused")
public class JmeRacePreparedEventPayloadExtractor implements PayloadExtractor<JmeRacePreparedEventPayload> {

    @Override
    public Set<EventData> getEventData(JmeRacePreparedEventPayload payload) {
        return Set.of(
                new EventData("race-id", payload.getRaceId()),
                new EventData("race-car-number", payload.getRaceCarNumber()));
    }

}
