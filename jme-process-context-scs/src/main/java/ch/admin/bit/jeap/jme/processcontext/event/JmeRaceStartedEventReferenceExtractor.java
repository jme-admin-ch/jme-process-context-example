package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.started.JmeRaceStartedEventReferences;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.message.ReferenceExtractor;

import java.util.Set;

public class JmeRaceStartedEventReferenceExtractor implements ReferenceExtractor<JmeRaceStartedEventReferences> {

    @Override
    public Set<MessageData> getMessageData(JmeRaceStartedEventReferences references) {
        String raceCarId = references.getRaceCarReference().getRaceCarId();
        String weatherAlertSubject = references.getWeatherAlertSubjectReference().getWeatherAlertSubject();

        return Set.of(
                MessageData.builder()
                        .key("raceCarId")
                        .value(raceCarId)
                        .role("RaceParticipant")
                        .build(),
                MessageData.builder()
                        .key("weatherAlertSubject")
                        .value(weatherAlertSubject)
                        .build());
    }
}
