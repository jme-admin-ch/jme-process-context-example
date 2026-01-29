package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.wather.alert.activated.WeatherAlertReferences;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageData;
import ch.admin.bit.jeap.processcontext.plugin.api.message.ReferenceExtractor;

import java.util.Set;

public class JmeRaceWeatherAlertActivatedReferenceExtractor implements ReferenceExtractor<WeatherAlertReferences> {

    @Override
    public Set<MessageData> getMessageData(WeatherAlertReferences references) {
        String weatherAlertSubject = references.getWeatherAlertReference().getWeatherAlertSubject();
        String weatherAlertId = references.getWeatherAlertReference().getWeatherAlertId();
        return Set.of(
                MessageData.builder()
                        .key("weatherAlertSubject")
                        .value(weatherAlertSubject)
                        .build(),
                MessageData.builder()
                        .key("weatherAlertId")
                        .value(weatherAlertId)
                        .build());
    }
}
