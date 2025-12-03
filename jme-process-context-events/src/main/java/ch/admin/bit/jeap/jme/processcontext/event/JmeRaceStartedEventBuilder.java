package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.started.*;
import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;

public class JmeRaceStartedEventBuilder extends AbstractTestEventBuilder<JmeRaceStartedEventBuilder, JmeRaceStartedEvent> {

    private static final String RACE_REF_TYPE = "race";
    private static final String RACE_CAR_REF_TYPE = "race";
    private static final String WEATHER_ALERT_SUBJECT = "weatherAlertSubject";

    private String raceId;
    private String raceCarId;
    private String weatherAlertSubject;

    private JmeRaceStartedEventBuilder(String originProcessId) {
        super(JmeRaceStartedEvent::new, JmeRaceStartedEventReferences::new, originProcessId);
    }

    public static JmeRaceStartedEventBuilder createForProcessId(String originProcessId) {
        return new JmeRaceStartedEventBuilder(originProcessId);
    }

    @Override
    protected final String getEventVersion() {
        return "1.1.0";
    }

    public JmeRaceStartedEventBuilder raceId(String raceId) {
        this.raceId = raceId;
        return this;
    }

    public JmeRaceStartedEventBuilder raceCarId(String raceCarId) {
        this.raceCarId = raceCarId;
        return this;
    }

    public JmeRaceStartedEventBuilder weatherAlertSubject(String weatherAlertSubject) {
        this.weatherAlertSubject = weatherAlertSubject;
        return this;
    }

    @Override
    public JmeRaceStartedEvent build() {
        if (raceId == null) {
            throw AvroMessageBuilderException.propertyNull("raceId");
        }
        if (raceCarId == null) {
            throw AvroMessageBuilderException.propertyNull("raceCarId");
        }
        if (weatherAlertSubject == null) {
            throw AvroMessageBuilderException.propertyNull("weatherAlertSubject");
        }

        JmeRaceStartedEvent event = super.build();
        JmeRaceStartedEventReferences references = JmeRaceStartedEventReferences.newBuilder()
                .setRaceReferenceBuilder(JmeRaceReference.newBuilder()
                        .setType(RACE_REF_TYPE)
                        .setRaceId(raceId))
                .setRaceCarReferenceBuilder(JmeRaceCarReference.newBuilder()
                        .setType(RACE_CAR_REF_TYPE)
                        .setRaceCarId(raceCarId))
                .setWeatherAlertSubjectReference(JmeWeatherAlertSubjectReference.newBuilder()
                        .setType(WEATHER_ALERT_SUBJECT)
                        .setWeatherAlertSubject(weatherAlertSubject)
                        .build())
                .build();
        event.setReferences(references);
        return event;
    }
}
