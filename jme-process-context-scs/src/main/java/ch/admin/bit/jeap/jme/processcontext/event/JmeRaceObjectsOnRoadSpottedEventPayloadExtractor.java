package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.objects.spotted.JmeRaceObjectsOnRoadSpottedEventPayload;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.message.PayloadExtractor;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class JmeRaceObjectsOnRoadSpottedEventPayloadExtractor implements PayloadExtractor<JmeRaceObjectsOnRoadSpottedEventPayload> {

    public static final String TRIGGER_SAFETY_CAR_KEY = "triggerSafetyCar";

    @Override
    public Set<MessageData> getMessageData(JmeRaceObjectsOnRoadSpottedEventPayload payload) {
        Optional<Boolean> optionalTriggerSafetyCar = payload.getOptionalTriggerSafetyCar();
        return optionalTriggerSafetyCar.map(triggerSafetyCar -> Set.of(new MessageData(TRIGGER_SAFETY_CAR_KEY, triggerSafetyCar.toString())))
                .orElse(Collections.emptySet());
    }
}
