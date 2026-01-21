package ch.admin.bit.jeap.jme.processcontext.domain;

import ch.admin.bit.jeap.processcontext.command.process.instance.create.ProcessData;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptyList;

@Getter
public class TestProcess implements Process {
    private static final String RACE_ID = "New Race Across Switzerland";

    private final MessagePublisher messagePublisher;
    private final String id;
    private final Set<String> reachedMilestones = new HashSet<>();
    private boolean processCompleted = false;

    public static TestProcess create(MessagePublisher messagePublisher, String id, String raceCarNumber, ProcessCreationType processCreationType) {
        TestProcess testProcess = new TestProcess(messagePublisher, id);

        if (ProcessCreationType.COMMAND.equals(processCreationType)) {
            messagePublisher.createProcessInstance(id, "raceProcess",
                    List.of(
                            new ProcessData("race-id", RACE_ID, null),
                            new ProcessData("race-car-number", raceCarNumber, null)
                    )
            );
        } else if (ProcessCreationType.EVENT.equals(processCreationType)) {
            messagePublisher.racePrepared(id, RACE_ID, raceCarNumber);
        }

        return testProcess;
    }

    public static void completeRaceCarPostChecks(MessagePublisher messagePublisher, List<String> raceCarNumbers) {
        messagePublisher.raceCarPostChecksCompleted(RACE_ID, raceCarNumbers);
    }

    public static void weatherAlertActivated(MessagePublisher messagePublisher, String weatherAlertSubject) {
        String weatherAlertId = UUID.randomUUID().toString();
        messagePublisher.weatherAlertActivated(weatherAlertSubject, weatherAlertId);
    }

    private TestProcess(MessagePublisher messagePublisher, String id) {
        this.messagePublisher = messagePublisher;
        this.id = id;
    }

    public static void carMaintenanceRequired(MessagePublisher messagePublisher, String processId, String maintenanceId) {
        messagePublisher.carMaintenanceRequired(processId, maintenanceId);
    }

    public static void carRefuellingCompleted(MessagePublisher messagePublisher, String processId) {
        messagePublisher.carRefuellingCompleted(processId);
    }

    public static void carTirePressureChecked(MessagePublisher messagePublisher, String processId, String maintenanceId, String tireDescription) {
        messagePublisher.carTirePressureChecked(processId, maintenanceId, tireDescription);
    }

    public static void cancelRace(MessagePublisher messagePublisher, String processId, String raceId) {
        messagePublisher.cancelRace(processId, raceId);
    }

    public void raceStarted(String weatherAlertSubject) {
        String raceCarId = UUID.randomUUID().toString();
        messagePublisher.raceStarted(id, RACE_ID, raceCarId, weatherAlertSubject);
    }

    public void planRaceControlPoints(String processId) {
        List<String> plannedControlPoints = List.of("Bern", "Brig", "Chur");
        messagePublisher.controlPointsPlanned(processId, plannedControlPoints);
    }

    public void raceControlpointPassed(String controlPoint) {
        messagePublisher.raceControlpointPassed(id, controlPoint);
    }

    public void raceDestinationReached() {
        messagePublisher.raceDestinationReached(id);
    }

    public void milestoneReached(String milestoneName) {
        reachedMilestones.add(milestoneName);
    }

    public void completed() {
        processCompleted = true;
    }

    public void raceValidated() {
        messagePublisher.raceValidated(id);
    }

    public void mobileCheckpointPassed(String checkpoint, String state, String taskId) {
        messagePublisher.mobileCheckpointPassed(id, checkpoint, state, taskId);
    }

    public void planRaceMobileCheckpoints(String processId) {
        messagePublisher.mobileCheckpointPlanned(processId, List.of("mobile-1", "mobile-2"));
    }

    public void planRaceCarPostChecks(String processId) {
        messagePublisher.raceCarPostChecksPlanned(processId, List.of("postchecks"));
    }

    public void skipRaceCarPostChecks(String processId) {
        messagePublisher.raceCarPostChecksPlanned(processId, emptyList());
    }

    public static void objectsOnTheRoadSpotted(MessagePublisher messagePublisher, String processId, boolean triggerSafetyCar) {
        messagePublisher.objectsOnTheRoadSpotted(processId, triggerSafetyCar);
    }
}
