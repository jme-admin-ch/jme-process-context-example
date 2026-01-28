package ch.admin.bit.jeap.jme.processcontext.kafka;

import ch.admin.bit.jeap.command.avro.AvroCommand;
import ch.admin.bit.jeap.domainevent.avro.AvroDomainEvent;
import ch.admin.bit.jeap.domainevent.avro.AvroDomainEventUser;
import ch.admin.bit.jeap.jme.processcontext.ProcessContextAppException;
import ch.admin.bit.jeap.jme.processcontext.command.race.cancel.JmeCancelRaceCommand;
import ch.admin.bit.jeap.jme.processcontext.domain.MessagePublisher;
import ch.admin.bit.jeap.jme.processcontext.event.*;
import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.refuelling.completed.JmeRaceCarRefuellingCompletedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.required.JmeRaceCarMaintenanceRequiredEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.tirepressure.checked.JmeRaceCarTirePressureCheckedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.carpostchecks.completed.JmeRaceCarPostCheckResult;
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
import ch.admin.bit.jeap.messaging.avro.AvroMessage;
import ch.admin.bit.jeap.messaging.avro.AvroMessageKey;
import ch.admin.bit.jme.document.JmeDocumentCreatedEvent;
import ch.admin.bit.jme.document.JmeDocumentReviewedEvent;
import ch.admin.bit.jme.document.JmeDocumentVersionCreatedEvent;
import ch.admin.bit.jme.document.JmeDocumentVersionReviewedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Component to publish to event for the process context service, updating the according test process
 */
@Component
@Slf4j
class KafkaMessagePublisher implements MessagePublisher {

    private final KafkaTemplate<AvroMessageKey, AvroMessage> bitClusterTemplate;

    private final KafkaTemplate<AvroMessageKey, AvroMessage> otherClusterTemplate;

    private final TopicConfiguration topicConfiguration;

    KafkaMessagePublisher(KafkaTemplate<AvroMessageKey, AvroMessage> bitClusterTemplate,
                          @Qualifier("other") KafkaTemplate<AvroMessageKey, AvroMessage> otherClusterTemplate,
                          TopicConfiguration topicConfiguration) {
        this.bitClusterTemplate = bitClusterTemplate;
        this.otherClusterTemplate = otherClusterTemplate;
        this.topicConfiguration = topicConfiguration;
    }

    private void send(final AvroDomainEvent event, String topic) {
        send(event, topic, null);
    }

    private void send(final AvroCommand command, String topic) {
        send(command, topic, null);
    }

    private void send(final AvroMessage message, String topic, String cluster) {
        log.debug("sending message='{}' with null key", message);
        try {
            if (cluster == null) {
                bitClusterTemplate.send(topic, message).get();
            } else if ("other".equalsIgnoreCase(cluster)){
                otherClusterTemplate.send(topic, message).get();
            } else {
                bitClusterTemplate.send(topic, message).get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ProcessContextAppException.cannotSendEvent(e);
        } catch (ExecutionException e) {
            throw ProcessContextAppException.cannotSendEvent(e);
        }
    }

    @Override
    public void racePrepared(String processId, String raceId, String raceCarNumber) {
        JmeRacePreparedEvent event = JmeRacePreparedEventBuilder.createForProcessId(processId)
                .raceId(raceId)
                .raceCarNumber(raceCarNumber)
                .idempotenceId(processId + "-" + raceId + "-" + raceCarNumber)
                .build();
        send(event, topicConfiguration.getRacePrepared());
    }

    @Override
    public void raceStarted(String processId, String raceId, String raceCarId, String weatherAlertSubject) {
        final AvroDomainEventUser eventUser = AvroDomainEventUser.newBuilder()
                .setFamilyName("Starter")
                .setGivenName("Max")
                .setId("maxId")
                .setPropertiesMap(Map.of("customsOffice", "Basel"))
                .build();
        JmeRaceStartedEvent event = JmeRaceStartedEventBuilder.createForProcessId(processId)
                .raceId(raceId)
                .raceCarId(raceCarId)
                .weatherAlertSubject(weatherAlertSubject)
                .idempotenceId(processId + "-" + raceId + "-" + raceCarId)
                .user(eventUser)
                .build();
        send(event, topicConfiguration.getRaceStarted());
    }

    @Override
    public void raceControlpointPassed(String processId, String controlPoint) {
        JmeRaceControlpointPassedEvent event = JmeRaceControlpointPassedEventBuilder.createForProcessId(processId)
                .controlPoint(controlPoint)
                .idempotenceId(processId + "-" + controlPoint)
                .build();
        send(event, topicConfiguration.getRaceControlpointPassed());

    }

    @Override
    public void raceDestinationReached(String processId) {
        final AvroDomainEventUser eventUser = AvroDomainEventUser.newBuilder()
                .setFamilyName("Reached")
                .setGivenName("Jack")
                .setId("jackId")
                .setPropertiesMap(Map.of("customsOffice", "London"))
                .build();
        JmeRaceDestinationReachedEvent event = JmeRaceDestinationReachedEventBuilder.createForProcessId(processId)
                .processReference(processId)
                .idempotenceId(processId + "-destination-reached")
                .user(eventUser)
                .parkingReference(7)
                .build();
        send(event, topicConfiguration.getRaceDestinationReached());
    }

    @Override
    public void mobileCheckpointPassed(String processId, String checkpoint, String state, String taskId) {
        JmeRaceMobileCheckpointPassedEvent event = JmeRaceMobileCheckpointPassedEventBuilder.createForProcessId(processId)
                .checkpoint(checkpoint)
                .state(state)
                .taskId(taskId)
                .controlDate(LocalDateTime.now())
                .idempotenceId(processId + "-" + taskId + "-" + state)
                .build();
        send(event, topicConfiguration.getRaceMobileCheckpointPassed());
    }

    @Override
    public void raceCarPostChecksCompleted(String raceId, List<String> raceCarNumbers) {
        List<JmeRaceCarPostCheckResult> results = raceCarNumbers.stream()
                .map(carNumber -> createJmeRaceCarPostCheckResult(carNumber, "Complies with the rules."))
                .toList();

        JmeRaceCarPostChecksCompletedEvent event = JmeRaceCarPostChecksCompletedEventBuilder.create()
                .raceId(raceId)
                .results(results)
                .idempotenceId(raceId + "-" + String.join("-", raceCarNumbers))
                .build();
        send(event, topicConfiguration.getRaceCarPostChecksCompleted());
    }

    private JmeRaceCarPostCheckResult createJmeRaceCarPostCheckResult(String raceCarNumber, String report) {
        return JmeRaceCarPostCheckResult.newBuilder()
                .setCarNumber(raceCarNumber)
                .setReport(report)
                .build();
    }

    @Override
    public void weatherAlertActivated(String weatherAlertSubject, String weatherAlertId) {
        JmeRaceWeatherAlertActivatedEvent event = JmeRaceWeatherAlertActivatedEventBuilder.create()
                .weatherAlertSubject(weatherAlertSubject)
                .weatherAlertId(weatherAlertId)
                .idempotenceId(weatherAlertSubject + "-" + weatherAlertId)
                .build();
        send(event, topicConfiguration.getWeatherAlertActivated());
    }

    @Override
    public void carMaintenanceRequired(String processId, String maintenanceId) {
        JmeRaceCarMaintenanceRequiredEvent event = JmeRaceCarMaintenanceRequiredEventBuilder.create(processId)
                .maintenanceId(maintenanceId)
                .idempotenceId(processId + "-" + maintenanceId)
                .build();
        send(event, topicConfiguration.getRaceCarMaintenanceRequired());
    }

    @Override
    public void carRefuellingCompleted(String processId) {
        JmeRaceCarRefuellingCompletedEvent event = JmeRaceCarRefuellingCompletedEventBuilder.create(processId)
                .idempotenceId(UUID.randomUUID().toString())
                .fuelType("gasoline")
                .fuelAmount(65)
                .build();
        send(event, topicConfiguration.getRaceCarRefuellingCompleted());
    }

    @Override
    public void carTirePressureChecked(String processId, String maintenanceId, String tireId) {
        JmeRaceCarTirePressureCheckedEvent event = JmeRaceCarTirePressureCheckedEventBuilder.create(processId)
                .maintenanceId(maintenanceId)
                .tireId(tireId)
                .idempotenceId(processId + "-" + maintenanceId + "-" + tireId)
                .build();
        send(event, topicConfiguration.getRaceCarTirePressureChecked());
    }

    @Override
    public void objectsOnTheRoadSpotted(String processId, boolean triggerSafetyCar) {
        JmeRaceObjectsOnRoadSpottedEvent event = JmeRaceObjectsOnRoadSpottedEventBuilder.createForProcessId(processId)
                .idempotenceId(UUID.randomUUID().toString()) //For simulating multiple events, the idempotenceId has to be different
                .triggerSafetyCar(triggerSafetyCar)
                .build();
        send(event, topicConfiguration.getObjectsOnRoadSpotted());
    }

    @Override
    public void mobileCheckpointPlanned(String processId, List<String> checkpoints) {
        JmeRaceMobileCheckpointPlannedEvent event = JmeRaceMobileCheckpointPlannedEventBuilder.createForProcessId(processId)
                .checkpoints(checkpoints)
                .idempotenceId(processId + "-" + String.join("-", checkpoints))
                .build();
        send(event, topicConfiguration.getRaceMobileCheckpointPlanned());
    }

    @Override
    public void controlPointsPlanned(String processId, List<String> controlPoints) {
        JmeRaceControlpointPlannedEvent event = JmeRaceControlpointPlannedEventBuilder.createForProcessId(processId)
                .controlPoints(controlPoints)
                .idempotenceId(processId + "-" + String.join("-", controlPoints))
                .build();
        send(event, topicConfiguration.getRaceControlpointPlanned(), "other");
    }

    @Override
    public void raceCarPostChecksPlanned(String processId, List<String> postChecks) {
        JmeRaceCarPostChecksPlannedEvent event = JmeRaceCarPostChecksPlannedEventBuilder.createForProcessId(processId)
                .postChecks(postChecks)
                .idempotenceId(processId + "-" + String.join("-", postChecks))
                .build();
        send(event, topicConfiguration.getRaceCarPostChecksPlanned());
    }

    @Override
    public void raceValidated(String processId) {
        final AvroDomainEventUser eventUser = AvroDomainEventUser.newBuilder()
                .setFamilyName("Validator")
                .setGivenName("Joe")
                .setId("joeId")
                .setPropertiesMap(Map.of("customsOffice", "Bern"))
                .build();
        JmeRaceValidatedEvent event = JmeRaceValidatedEventBuilder.createForProcessId(processId)
                .idempotenceId(processId + "-validated")
                .user(eventUser)
                .build();
        send(event, topicConfiguration.getRaceValidated());
    }

    @Override
    public void cancelRace(String processId, String raceId) {
        JmeCancelRaceCommand command = JmeCancelRaceCommandBuilder.builder(processId)
                .raceId(raceId)
                .idempotenceId(processId + "-cancel-race")
                .build();
        send(command, topicConfiguration.getCancelRace());
    }

    @Override
    public void documentCreated(String processId, String documentId, String title, String author) {
        JmeDocumentCreatedEvent event = JmeDocumentCreatedEventBuilder.createForProcessId(processId)
                .documentId(documentId)
                .title(title)
                .author(author)
                .idempotenceId(processId + "-document-created")
                .build();
        send(event, topicConfiguration.getDocumentCreated());
    }

    @Override
    public void documentReviewCreated(String processId, String documentId, String status) {
        JmeDocumentReviewedEvent event = JmeDocumentReviewedEventBuilder.createForProcessId(processId)
                .documentId(documentId)
                .status(status)
                .idempotenceId("%s-%s-document-reviewed".formatted(processId, documentId))
                .build();
        send(event, topicConfiguration.getDocumentReviewed());
    }

    @Override
    public void documentVersionCreated(String processId, String versionId, String versionNumber, String changes) {
        JmeDocumentVersionCreatedEvent event = JmeDocumentVersionCreatedEventBuilder.createForProcessId(processId)
                .versionId(versionId)
                .versionNumber(versionNumber)
                .changes(changes)
                .idempotenceId("%s-%s-version-%s-created".formatted(processId, versionId, versionNumber))
                .build();
        send(event, topicConfiguration.getDocumentVersionCreated());
    }

    @Override
    public void documentVersionReviewed(String processId, String reviewId, String versionId, String versionNumber, String status, String comments) {
        JmeDocumentVersionReviewedEvent event = JmeDocumentVersionReviewedEventBuilder.createForProcessId(processId)
                .reviewId(reviewId)
                .versionId(versionId)
                .versionNumber(versionNumber)
                .status(status)
                .comments(comments)
                .idempotenceId(UUID.randomUUID().toString()) // We assume the review can change
                .build();
        send(event, topicConfiguration.getDocumentVersionReviewed());
    }

}
