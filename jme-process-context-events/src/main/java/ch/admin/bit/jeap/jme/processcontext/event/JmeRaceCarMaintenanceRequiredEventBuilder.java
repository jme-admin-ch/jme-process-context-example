package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.required.JmeRaceCarMaintenanceRequiredEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.required.JmeRaceCarMaintenanceRequiredEventReferences;
import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;
import ch.admin.bit.jme.processcontext.race.maintenance.MaintenanceReference;

public class JmeRaceCarMaintenanceRequiredEventBuilder extends AbstractTestEventBuilder<JmeRaceCarMaintenanceRequiredEventBuilder, JmeRaceCarMaintenanceRequiredEvent> {

    private String maintenanceId;

    private JmeRaceCarMaintenanceRequiredEventBuilder(String originProcessId) {
        super(JmeRaceCarMaintenanceRequiredEvent::new, JmeRaceCarMaintenanceRequiredEventReferences::new, originProcessId);
    }

    public static JmeRaceCarMaintenanceRequiredEventBuilder create(String processId) {
        return new JmeRaceCarMaintenanceRequiredEventBuilder(processId);
    }

    public JmeRaceCarMaintenanceRequiredEventBuilder maintenanceId(String maintenanceId) {
        this.maintenanceId = maintenanceId;
        return this;
    }

    @Override
    public JmeRaceCarMaintenanceRequiredEvent build() {
        if (maintenanceId == null) {
            throw AvroMessageBuilderException.propertyNull("maintenanceId");
        }
        JmeRaceCarMaintenanceRequiredEvent event = super.build();
        JmeRaceCarMaintenanceRequiredEventReferences references = JmeRaceCarMaintenanceRequiredEventReferences.newBuilder()
                .setMaintenanceReference(MaintenanceReference.newBuilder()
                        .setMaintenanceId(maintenanceId)
                        .build())
                .build();
        event.setReferences(references);
        return event;
    }
}
