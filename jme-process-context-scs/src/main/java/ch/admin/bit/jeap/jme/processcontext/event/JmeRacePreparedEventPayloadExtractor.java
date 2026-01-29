package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.prepared.JmeRacePreparedEventPayload;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.message.PayloadExtractor;

import java.util.Set;

@SuppressWarnings("unused")
public class JmeRacePreparedEventPayloadExtractor implements PayloadExtractor<JmeRacePreparedEventPayload> {

    @Override
    public Set<MessageData> getMessageData(JmeRacePreparedEventPayload payload) {
        return Set.of(
                new MessageData("race-id", payload.getRaceId()),
                new MessageData("race-car-number", payload.getRaceCarNumber()));
    }

}
