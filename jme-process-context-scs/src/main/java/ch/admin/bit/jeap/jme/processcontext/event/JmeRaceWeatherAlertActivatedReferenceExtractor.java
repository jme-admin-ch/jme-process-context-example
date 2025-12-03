package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.wather.alert.activated.WeatherAlertReferences;
import ch.admin.bit.jeap.processcontext.plugin.api.event.EventData;
import ch.admin.bit.jeap.processcontext.plugin.api.event.ReferenceExtractor;

import java.util.Set;

public class JmeRaceWeatherAlertActivatedReferenceExtractor implements ReferenceExtractor<WeatherAlertReferences> {

    @Override
    public Set<EventData> getEventData(WeatherAlertReferences references) {
        String weatherAlertSubject = references.getWeatherAlertReference().getWeatherAlertSubject();
        String weatherAlertId = references.getWeatherAlertReference().getWeatherAlertId();
        return Set.of(
                EventData.builder()
                        .key("weatherAlertSubject")
                        .value(weatherAlertSubject)
                        .build(),
                EventData.builder()
                        .key("weatherAlertId")
                        .value(weatherAlertId)
                        .build());
    }
}
