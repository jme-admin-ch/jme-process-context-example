package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.mobilecheckpoint.passed.JmeRaceMobileCheckpointPassedEventReferences;
import ch.admin.bit.jeap.jme.processcontext.event.race.mobilecheckpoint.passed.JmeRaceMobileCheckpointReference;
import ch.admin.bit.jeap.processcontext.plugin.api.event.EventData;
import ch.admin.bit.jeap.processcontext.plugin.api.event.ReferenceExtractor;

import java.util.Set;

public class JmeRaceMobileCheckpointPassedEventReferenceExtractor implements ReferenceExtractor<JmeRaceMobileCheckpointPassedEventReferences> {

    @Override
    public Set<EventData> getEventData(JmeRaceMobileCheckpointPassedEventReferences references) {
        JmeRaceMobileCheckpointReference checkpointReference = references.getCheckpoint();
        EventData checkpointEventData = EventData.builder()
                .key("mobileCheckpoint")
                .value(checkpointReference.getCheckpoint())
                .role("PassedCheckpoint")
                .build();
        EventData checkpointTypeEventData = EventData.builder()
                .key("type")
                .value(checkpointReference.getType())
                .build();
        return Set.of(checkpointEventData, checkpointTypeEventData);
    }
}
