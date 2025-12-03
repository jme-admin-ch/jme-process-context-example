package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.mobilecheckpoint.passed.JmeRaceMobileCheckpointPassedEventPayload;
import ch.admin.bit.jeap.processcontext.plugin.api.event.EventData;
import ch.admin.bit.jeap.processcontext.plugin.api.event.PayloadExtractor;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class JmeRaceMobileCheckpointPassedEventPayloadExtractor implements PayloadExtractor<JmeRaceMobileCheckpointPassedEventPayload> {

    @Override
    public Set<EventData> getEventData(JmeRaceMobileCheckpointPassedEventPayload payload) {
        return Set.of(
                new EventData("taskId", payload.getTaskId()),
                new EventData("state", payload.getState()),
                new EventData("date", payload.getControlDate().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME)));
    }
}
