package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.validated.JmeRaceValidatedEvent;

public class JmeRaceValidatedEventBuilder extends AbstractTestEventBuilder<JmeRaceValidatedEventBuilder, JmeRaceValidatedEvent> {

    private JmeRaceValidatedEventBuilder(String originProcessId) {
        super(JmeRaceValidatedEvent::new, originProcessId);
    }

    public static JmeRaceValidatedEventBuilder createForProcessId(String originProcessId) {
        return new JmeRaceValidatedEventBuilder(originProcessId);
    }

    @Override
    public JmeRaceValidatedEvent build() {
        return super.build();
    }

}
