package ch.admin.bit.jeap.jme.processcontext.event;


import ch.admin.bit.jeap.jme.processcontext.event.race.destination.reached.JmeRaceDestinationReachedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.destination.reached.JmeRaceDestinationReachedEventReferences;
import ch.admin.bit.jeap.jme.processcontext.event.race.destination.reached.ParkingReference;
import ch.admin.bit.jeap.jme.processcontext.event.race.destination.reached.ProcessReference;

public class JmeRaceDestinationReachedEventBuilder extends AbstractTestEventBuilder<JmeRaceDestinationReachedEventBuilder, JmeRaceDestinationReachedEvent> {

    private ProcessReference processReference;
    private ParkingReference parkingReference;

    private JmeRaceDestinationReachedEventBuilder(String originProcessId) {
        super(JmeRaceDestinationReachedEvent::new, JmeRaceDestinationReachedEventReferences::new, originProcessId);
    }

    public static JmeRaceDestinationReachedEventBuilder createForProcessId(String originProcessId) {
        return new JmeRaceDestinationReachedEventBuilder(originProcessId);
    }

    public JmeRaceDestinationReachedEventBuilder processReference(String processReference) {
        this.processReference = new ProcessReference("processReference", processReference);
        return this;
    }

    public JmeRaceDestinationReachedEventBuilder parkingReference(int parkingSpotNumber) {
        this.parkingReference = new ParkingReference("parkingReference", parkingSpotNumber);
        return this;
    }

    @Override
    public JmeRaceDestinationReachedEvent build() {
        JmeRaceDestinationReachedEvent event = super.build();
        JmeRaceDestinationReachedEventReferences references = new JmeRaceDestinationReachedEventReferences();
        references.setProcessReference(processReference);
        references.setParkingReference(parkingReference);
        event.setReferences(references);
        return event;
    }

}
