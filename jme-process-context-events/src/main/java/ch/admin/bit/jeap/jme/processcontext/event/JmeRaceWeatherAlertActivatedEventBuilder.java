package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.wather.alert.activated.JmeRaceWeatherAlertActivatedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.wather.alert.activated.WeatherAlertReference;
import ch.admin.bit.jeap.jme.processcontext.event.race.wather.alert.activated.WeatherAlertReferences;
import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;

public class JmeRaceWeatherAlertActivatedEventBuilder extends AbstractTestEventBuilder<JmeRaceWeatherAlertActivatedEventBuilder, JmeRaceWeatherAlertActivatedEvent> {

    private String weatherAlertSubject;
    private String weatherAlertId;

    private JmeRaceWeatherAlertActivatedEventBuilder(String originProcessId) {
        super(JmeRaceWeatherAlertActivatedEvent::new, WeatherAlertReferences::new, originProcessId);
    }

    public static JmeRaceWeatherAlertActivatedEventBuilder create() {
        return new JmeRaceWeatherAlertActivatedEventBuilder(null);
    }

    @Override
    protected final String getEventVersion() {
        return "1.1.0";
    }

    public JmeRaceWeatherAlertActivatedEventBuilder weatherAlertSubject(String weatherAlertSubject) {
        this.weatherAlertSubject = weatherAlertSubject;
        return this;
    }

    public JmeRaceWeatherAlertActivatedEventBuilder weatherAlertId(String weatherAlertId) {
        this.weatherAlertId = weatherAlertId;
        return this;
    }

    @Override
    public JmeRaceWeatherAlertActivatedEvent build() {
        if (weatherAlertSubject == null) {
            throw AvroMessageBuilderException.propertyNull("weatherAlertSubject");
        }
        if (weatherAlertId == null) {
            throw AvroMessageBuilderException.propertyNull("weatherAlertId");
        }

        JmeRaceWeatherAlertActivatedEvent event = super.build();
        WeatherAlertReferences references = WeatherAlertReferences.newBuilder()
                .setWeatherAlertReferenceBuilder(WeatherAlertReference.newBuilder()
                        .setWeatherAlertSubject(weatherAlertSubject)
                        .setWeatherAlertId(weatherAlertId))
                .build();
        event.setReferences(references);
        return event;
    }
}
