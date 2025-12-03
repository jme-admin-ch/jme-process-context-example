package ch.admin.bit.jeap.jme.processcontext.event;

import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.tirepressure.checked.JmeRaceCarTirePressureCheckedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.tirepressure.checked.JmeRaceCarTirePressureCheckedEventReferences;
import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.tirepressure.checked.TireReference;
import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;
import ch.admin.bit.jme.processcontext.race.maintenance.MaintenanceReference;

public class JmeRaceCarTirePressureCheckedEventBuilder extends AbstractTestEventBuilder<JmeRaceCarTirePressureCheckedEventBuilder, JmeRaceCarTirePressureCheckedEvent> {

    private String maintenanceId;
    private String tireId;

    private JmeRaceCarTirePressureCheckedEventBuilder(String originProcessId) {
        super(JmeRaceCarTirePressureCheckedEvent::new, JmeRaceCarTirePressureCheckedEventReferences::new, originProcessId);
    }

    public static JmeRaceCarTirePressureCheckedEventBuilder create(String processId) {
        return new JmeRaceCarTirePressureCheckedEventBuilder(processId);
    }

    public JmeRaceCarTirePressureCheckedEventBuilder maintenanceId(String maintenanceId) {
        this.maintenanceId = maintenanceId;
        return this;
    }

    public JmeRaceCarTirePressureCheckedEventBuilder tireId(String tireId) {
        this.tireId = tireId;
        return this;
    }

    @Override
    public JmeRaceCarTirePressureCheckedEvent build() {
        if (maintenanceId == null) {
            throw AvroMessageBuilderException.propertyNull("maintenanceId");
        }
        if (tireId == null) {
            throw AvroMessageBuilderException.propertyNull("tireId");
        }

        JmeRaceCarTirePressureCheckedEvent event = super.build();
        JmeRaceCarTirePressureCheckedEventReferences references = JmeRaceCarTirePressureCheckedEventReferences.newBuilder()
                .setMaintenanceReference(MaintenanceReference.newBuilder()
                        .setMaintenanceId(maintenanceId)
                        .build())
                .setTireReference(TireReference.newBuilder()
                        .setTireId(tireId)
                        .build())
                .build();
        event.setReferences(references);
        return event;
    }
}
