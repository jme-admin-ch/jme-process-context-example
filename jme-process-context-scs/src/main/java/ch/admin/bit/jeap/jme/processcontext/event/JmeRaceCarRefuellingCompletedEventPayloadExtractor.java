package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.refuelling.completed.JmeRaceCarRefuellingCompletedEventPayload;
import ch.admin.bit.jeap.processcontext.plugin.api.event.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.event.PayloadExtractor;

import java.util.Set;

import static org.springframework.util.StringUtils.hasText;

public class JmeRaceCarRefuellingCompletedEventPayloadExtractor implements PayloadExtractor<JmeRaceCarRefuellingCompletedEventPayload> {

    @Override
    public Set<MessageData> getMessageData(JmeRaceCarRefuellingCompletedEventPayload payload) {
        if (hasText(payload.getFuelType())) {
            return Set.of(
                    MessageData.builder()
                            .key("fuelType")
                            .value(payload.getFuelType())
                            .build(),
                    MessageData.builder()
                            .key("fuelAmount")
                            .value(Integer.toString(payload.getFuelAmount()))
                            .build()
            );
        } else {
            return Set.of();
        }
    }

}
