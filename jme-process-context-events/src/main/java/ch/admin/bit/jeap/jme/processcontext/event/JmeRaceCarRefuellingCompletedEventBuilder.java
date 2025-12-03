package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.refuelling.completed.JmeRaceCarRefuellingCompletedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.refuelling.completed.JmeRaceCarRefuellingCompletedEventPayload;
import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.refuelling.completed.JmeRaceCarRefuellingCompletedEventReferences;

public class JmeRaceCarRefuellingCompletedEventBuilder extends AbstractTestEventBuilder<JmeRaceCarRefuellingCompletedEventBuilder, JmeRaceCarRefuellingCompletedEvent> {

    private String fuelType;
    private int fuelAmount;

    public JmeRaceCarRefuellingCompletedEventBuilder fuelType(String fuelType) {
        this.fuelType = fuelType;
        return this;
    }

    public JmeRaceCarRefuellingCompletedEventBuilder fuelAmount(int fuelAmount) {
        this.fuelAmount = fuelAmount;
        return this;
    }

    private JmeRaceCarRefuellingCompletedEventBuilder(String originProcessId) {
        super(JmeRaceCarRefuellingCompletedEvent::new, JmeRaceCarRefuellingCompletedEventReferences::new, originProcessId);
    }

    public static JmeRaceCarRefuellingCompletedEventBuilder create(String processId) {
        return new JmeRaceCarRefuellingCompletedEventBuilder(processId);
    }

    @Override
    public JmeRaceCarRefuellingCompletedEvent build() {
        JmeRaceCarRefuellingCompletedEvent event = super.build();
        event.setReferences(JmeRaceCarRefuellingCompletedEventReferences.newBuilder().build());
        event.setPayload(JmeRaceCarRefuellingCompletedEventPayload.newBuilder().
                setFuelType(fuelType).
                setFuelAmount(fuelAmount).
                build());
        return event;
    }
}
