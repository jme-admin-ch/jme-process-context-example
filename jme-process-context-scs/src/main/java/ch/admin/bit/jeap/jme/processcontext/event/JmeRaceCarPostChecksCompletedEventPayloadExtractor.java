package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.carpostchecks.completed.JmeRaceCarPostChecksCompletedEventPayload;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.message.PayloadExtractor;

import java.util.Set;
import java.util.stream.Collectors;

public class JmeRaceCarPostChecksCompletedEventPayloadExtractor implements PayloadExtractor<JmeRaceCarPostChecksCompletedEventPayload> {

    @Override
    public Set<MessageData> getMessageData(JmeRaceCarPostChecksCompletedEventPayload payload) {

        return payload.getResults().stream().map(r -> new MessageData("race-car-number", r.getCarNumber())).collect(Collectors.toSet());

    }
}
