package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.objects.spotted.JmeRaceObjectsOnRoadSpottedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.objects.spotted.JmeRaceObjectsOnRoadSpottedEventPayload;
import ch.admin.bit.jeap.jme.processcontext.event.race.objects.spotted.JmeRaceObjectsOnRoadSpottedEventReferences;

public class JmeRaceObjectsOnRoadSpottedEventBuilder extends AbstractTestEventBuilder<JmeRaceObjectsOnRoadSpottedEventBuilder, JmeRaceObjectsOnRoadSpottedEvent> {

    private Boolean triggerSafetyCar;

   private JmeRaceObjectsOnRoadSpottedEventBuilder(String originProcessId) {
        super(JmeRaceObjectsOnRoadSpottedEvent::new, JmeRaceObjectsOnRoadSpottedEventReferences::new, originProcessId);
    }

    public static JmeRaceObjectsOnRoadSpottedEventBuilder createForProcessId(String originProcessId) {
        return new JmeRaceObjectsOnRoadSpottedEventBuilder(originProcessId);
    }

    public JmeRaceObjectsOnRoadSpottedEventBuilder triggerSafetyCar(boolean triggerSafetyCar) {
       this.triggerSafetyCar = triggerSafetyCar;
       return this;
    }

    @Override
    public JmeRaceObjectsOnRoadSpottedEvent build() {

        JmeRaceObjectsOnRoadSpottedEvent event = super.build();
        JmeRaceObjectsOnRoadSpottedEventReferences references = JmeRaceObjectsOnRoadSpottedEventReferences
                .newBuilder()
                .build();

        event.setReferences(references);
        if (triggerSafetyCar != null) {
            JmeRaceObjectsOnRoadSpottedEventPayload.Builder payload = JmeRaceObjectsOnRoadSpottedEventPayload.newBuilder();
            payload.setTriggerSafetyCar(triggerSafetyCar);
            event.setPayload(payload.build());
        }
        return event;
    }


}
