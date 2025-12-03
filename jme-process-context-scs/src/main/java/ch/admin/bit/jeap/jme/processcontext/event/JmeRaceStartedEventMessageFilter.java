package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.started.JmeRaceStartedEvent;
import ch.admin.bit.jeap.processcontext.plugin.api.message.MessageFilter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JmeRaceStartedEventMessageFilter implements MessageFilter<JmeRaceStartedEvent> {

    @Override
    public boolean filter(JmeRaceStartedEvent message) {
        log.info("Filtering JmeRaceStartedEvent Event: {}", message);
        if (message.getReferences().getWeatherAlertSubjectReference() != null && message.getReferences().getWeatherAlertSubjectReference().getWeatherAlertSubject().toLowerCase().contains("filter")) {
            log.info("WeatherAlertSubjectReference '{}' contains 'filter': ignoring message", message.getReferences().getWeatherAlertSubjectReference());
            return false;
        }
        return true;
    }
}
