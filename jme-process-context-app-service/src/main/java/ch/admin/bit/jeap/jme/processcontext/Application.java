package ch.admin.bit.jeap.jme.processcontext;

import ch.admin.bit.jeap.jme.processcontext.command.race.cancel.JmeCancelRaceCommand;
import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.refuelling.completed.JmeRaceCarRefuellingCompletedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.required.JmeRaceCarMaintenanceRequiredEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.tirepressure.checked.JmeRaceCarTirePressureCheckedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.carpostchecks.completed.JmeRaceCarPostChecksCompletedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.carpostchecks.planned.JmeRaceCarPostChecksPlannedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.controlpoint.passed.JmeRaceControlpointPassedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.controlpoint.planned.JmeRaceControlpointPlannedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.destination.reached.JmeRaceDestinationReachedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.mobilecheckpoint.passed.JmeRaceMobileCheckpointPassedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.mobilecheckpoint.planned.JmeRaceMobileCheckpointPlannedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.objects.spotted.JmeRaceObjectsOnRoadSpottedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.prepared.JmeRacePreparedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.started.JmeRaceStartedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.validated.JmeRaceValidatedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.wather.alert.activated.JmeRaceWeatherAlertActivatedEvent;
import ch.admin.bit.jeap.messaging.annotations.JeapMessageProducerContract;
import ch.admin.bit.jeap.messaging.annotations.JeapMessageProducerContracts;
import ch.admin.bit.jeap.processcontext.command.process.instance.create.CreateProcessInstanceCommand;
import ch.admin.bit.jme.document.JmeDocumentCreatedEvent;
import ch.admin.bit.jme.document.JmeDocumentReviewedEvent;
import ch.admin.bit.jme.document.JmeDocumentVersionCreatedEvent;
import ch.admin.bit.jme.document.JmeDocumentVersionReviewedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
@JeapMessageProducerContract(value = CreateProcessInstanceCommand.TypeRef.class, topic = "jme-process-createprocessinstance")
@JeapMessageProducerContracts({
        JmeRaceStartedEvent.TypeRef.class,
        JmeRaceDestinationReachedEvent.TypeRef.class,
        JmeRaceWeatherAlertActivatedEvent.TypeRef.class,
        JmeRaceControlpointPlannedEvent.TypeRef.class,
        JmeRaceMobileCheckpointPlannedEvent.TypeRef.class,
        JmeRaceCarPostChecksPlannedEvent.TypeRef.class,
        JmeRaceCarMaintenanceRequiredEvent.TypeRef.class,
        JmeRaceCarTirePressureCheckedEvent.TypeRef.class,
        JmeRaceCarRefuellingCompletedEvent.TypeRef.class,
        JmeRaceObjectsOnRoadSpottedEvent.TypeRef.class,
        JmeRaceValidatedEvent.TypeRef.class,
        JmeRacePreparedEvent.TypeRef.class,
        JmeRaceControlpointPassedEvent.TypeRef.class,
        JmeRaceMobileCheckpointPassedEvent.TypeRef.class,
        JmeRaceCarPostChecksCompletedEvent.TypeRef.class,
        JmeCancelRaceCommand.TypeRef.class,
        JmeDocumentCreatedEvent.TypeRef.class,
        JmeDocumentVersionCreatedEvent.TypeRef.class,
        JmeDocumentReviewedEvent.TypeRef.class,
        JmeDocumentVersionReviewedEvent.TypeRef.class
})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
