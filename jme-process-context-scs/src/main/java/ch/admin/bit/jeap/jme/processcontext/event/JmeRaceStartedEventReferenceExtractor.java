package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.started.JmeRaceStartedEventReferences;
import ch.admin.bit.jeap.processcontext.plugin.api.event.EventData;
import ch.admin.bit.jeap.processcontext.plugin.api.event.ReferenceExtractor;

import java.util.Set;

public class JmeRaceStartedEventReferenceExtractor implements ReferenceExtractor<JmeRaceStartedEventReferences> {

    @Override
    public Set<EventData> getEventData(JmeRaceStartedEventReferences references) {
        String raceCarId = references.getRaceCarReference().getRaceCarId();
        String weatherAlertSubject = references.getWeatherAlertSubjectReference().getWeatherAlertSubject();

        return Set.of(
                EventData.builder()
                        .key("raceCarId")
                        .value(raceCarId)
                        .role("RaceParticipant")
                        .build(),
                EventData.builder()
                        .key("weatherAlertSubject")
                        .value(weatherAlertSubject)
                        .build());
    }
}
