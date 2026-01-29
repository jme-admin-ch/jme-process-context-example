package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.mobilecheckpoint.passed.JmeRaceMobileCheckpointPassedEventPayload;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.message.PayloadExtractor;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class JmeRaceMobileCheckpointPassedEventPayloadExtractor implements PayloadExtractor<JmeRaceMobileCheckpointPassedEventPayload> {

    @Override
    public Set<MessageData> getMessageData(JmeRaceMobileCheckpointPassedEventPayload payload) {
        return Set.of(
                new MessageData("taskId", payload.getTaskId()),
                new MessageData("state", payload.getState()),
                new MessageData("date", payload.getControlDate().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME)));
    }
}
