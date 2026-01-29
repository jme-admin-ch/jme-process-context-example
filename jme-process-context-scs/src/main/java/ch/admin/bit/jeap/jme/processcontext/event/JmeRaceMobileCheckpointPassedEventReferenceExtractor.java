package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.mobilecheckpoint.passed.JmeRaceMobileCheckpointPassedEventReferences;
import ch.admin.bit.jeap.jme.processcontext.event.race.mobilecheckpoint.passed.JmeRaceMobileCheckpointReference;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.message.ReferenceExtractor;

import java.util.Set;

public class JmeRaceMobileCheckpointPassedEventReferenceExtractor implements ReferenceExtractor<JmeRaceMobileCheckpointPassedEventReferences> {

    @Override
    public Set<MessageData> getMessageData(JmeRaceMobileCheckpointPassedEventReferences references) {
        JmeRaceMobileCheckpointReference checkpointReference = references.getCheckpoint();
        MessageData checkpointEventData = MessageData.builder()
                .key("mobileCheckpoint")
                .value(checkpointReference.getCheckpoint())
                .role("PassedCheckpoint")
                .build();
        MessageData checkpointTypeEventData = MessageData.builder()
                .key("type")
                .value(checkpointReference.getType())
                .build();
        return Set.of(checkpointEventData, checkpointTypeEventData);
    }
}
