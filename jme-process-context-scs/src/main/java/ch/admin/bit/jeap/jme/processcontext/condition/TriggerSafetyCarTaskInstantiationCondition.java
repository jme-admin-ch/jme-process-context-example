package ch.admin.bit.jeap.jme.processcontext.condition;

import ch.admin.bit.jeap.processcontext.plugin.api.condition.TaskInstantiationCondition;
import ch.admin.bit.jeap.processcontext.plugin.api.context.Message;

import static ch.admin.bit.jeap.jme.processcontext.event.JmeRaceObjectsOnRoadSpottedEventPayloadExtractor.TRIGGER_SAFETY_CAR_KEY;

public class TriggerSafetyCarTaskInstantiationCondition implements TaskInstantiationCondition
{
    @Override
    public boolean instantiate(Message message) {
        return message.getMessageData().stream().anyMatch(messageData ->
                messageData.getKey().equals(TRIGGER_SAFETY_CAR_KEY)
                && messageData.getValue().equalsIgnoreCase("true"));
    }
}
