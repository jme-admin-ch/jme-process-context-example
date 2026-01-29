package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.controlpoint.passed.JmeRaceControlpointPassedEventReferences;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.message.ReferenceExtractor;

import java.util.Set;

public class JmeRaceControlpointPassedEventReferenceExtractor implements ReferenceExtractor<JmeRaceControlpointPassedEventReferences> {

    @Override
    public Set<MessageData> getMessageData(JmeRaceControlpointPassedEventReferences references) {
        String controlpoint = references.getControlPoint().getControlPoint();
        MessageData raceCarIdEventData = MessageData.builder()
                .key("controlpoint")
                .value(controlpoint)
                .build();
        return Set.of(raceCarIdEventData);
    }
}
