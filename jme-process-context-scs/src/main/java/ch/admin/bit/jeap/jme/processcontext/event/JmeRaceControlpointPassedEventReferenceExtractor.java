package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.controlpoint.passed.JmeRaceControlpointPassedEventReferences;
import ch.admin.bit.jeap.processcontext.plugin.api.event.EventData;
import ch.admin.bit.jeap.processcontext.plugin.api.event.ReferenceExtractor;

import java.util.Set;

public class JmeRaceControlpointPassedEventReferenceExtractor implements ReferenceExtractor<JmeRaceControlpointPassedEventReferences> {

    @Override
    public Set<EventData> getEventData(JmeRaceControlpointPassedEventReferences references) {
        String controlpoint = references.getControlPoint().getControlPoint();
        EventData raceCarIdEventData = EventData.builder()
                .key("controlpoint")
                .value(controlpoint)
                .build();
        return Set.of(raceCarIdEventData);
    }
}
