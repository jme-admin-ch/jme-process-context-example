package ch.admin.bit.jeap.jme.processcontext.domain;

import ch.admin.bit.jeap.jme.processcontext.event.race.carmaintenance.refuelling.completed.JmeRaceCarRefuellingCompletedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.started.JmeRaceStartedEvent;
import ch.admin.bit.jeap.jme.processcontext.event.race.validated.JmeRaceValidatedEvent;

import java.util.List;

public interface MessagePublisher {

    void racePrepared(String processId, String raceId, String raceCarNumber);

    JmeRaceStartedEvent raceStarted(String processId, String raceId, String raceCarId, String weatherAlertSubject);

    void raceControlpointPassed(String processId, String controlPoint);

    void raceDestinationReached(String processId);

    void mobileCheckpointPassed(String processId, String controlPoint, String state, String taskId);

    void raceCarPostChecksCompleted(String raceId, List<String> raceCarNumbers);

    void weatherAlertActivated(String weatherAlertSubject, String weatherAlertId);

    void carMaintenanceRequired(String processId, String maintenanceId);

    JmeRaceCarRefuellingCompletedEvent carRefuellingCompleted(String processId);

    void carTirePressureChecked(String processId, String maintenanceId, String tireDescription);

    void objectsOnTheRoadSpotted(String processId, boolean triggerSafetyCar);

    void mobileCheckpointPlanned(String processId, List<String> checkpoints);

    void controlPointsPlanned(String processId, List<String> controlpoints);

    void raceCarPostChecksPlanned(String processId, List<String> postChecks);

    JmeRaceValidatedEvent raceValidated(String id);

    void cancelRace(String processId, String raceId);

    void documentCreated(String processId, String documentId, String title, String author);

    void documentReviewCreated(String processId, String documentId, String status);

    void documentVersionCreated(String processId, String versionId, String versionNumber, String changes);

    void documentVersionReviewed(String processId, String reviewId, String versionId, String versionNumber, String status, String comments);
}
