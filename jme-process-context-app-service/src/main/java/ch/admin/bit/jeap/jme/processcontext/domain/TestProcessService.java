package ch.admin.bit.jeap.jme.processcontext.domain;

import ch.admin.bit.jeap.jme.processcontext.ProcessContextAppException;
import ch.admin.bit.jeap.jme.processcontext.web.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class TestProcessService {
    private final TestProcessRepository testProcessRepository;
    private final MessagePublisher messagePublisher;

    public void create(String processId, NewProcessDTO newProcessDTO) {
        TestProcess testProcess = TestProcess.create(messagePublisher, processId, newProcessDTO.getRaceCarNumber());
        testProcessRepository.save(testProcess);
    }

    public void createDocumentReviewProcess(String processId, NewDocumentReviewProcessDTO newDocumentReviewProcessDTO) {
        DocumentReviewProcess process = DocumentReviewProcess.create(messagePublisher, processId, newDocumentReviewProcessDTO);
        testProcessRepository.save(process);
    }

    public void raceStarted(String processId, String weatherAlertSubject) {
        TestProcess testProcess = get(processId);
        testProcess.raceStarted(weatherAlertSubject);
        testProcess.planRaceControlPoints(processId);
        testProcess.planRaceMobileCheckpoints(processId);
    }

    public TestProcess get(String processId) {
        return testProcessRepository.findById(processId, TestProcess.class)
                .orElseThrow(() -> ProcessContextAppException.notFound(processId));
    }

    public Optional<Process> findById(String processId) {
        return testProcessRepository.findById(processId, Process.class);
    }

    public void raceControlpointPassed(String processId, String controlPoint) {
        TestProcess testProcess = get(processId);
        testProcess.raceControlpointPassed(controlPoint);
    }

    public void mobileCheckpointPassed(String processId, String checkpoint, String state, String taskId) {
        TestProcess testProcess = get(processId);
        testProcess.mobileCheckpointPassed(checkpoint, state, taskId);
    }

    public void planRaceCarPostChecks(String processId) {
        TestProcess testProcess = get(processId);
        testProcess.planRaceCarPostChecks(processId);
    }

    public void objectsOnTheRoadSpotted(String processId, boolean triggerSafetyCar) {
        TestProcess.objectsOnTheRoadSpotted(messagePublisher, processId, triggerSafetyCar);
    }

    public void carMaintenanceRequired(String processId, String maintenanceId) {
        TestProcess.carMaintenanceRequired(messagePublisher, processId, maintenanceId);
    }

    public void carRefuellingCompleted(String processId) {
        TestProcess.carRefuellingCompleted(messagePublisher, processId);
    }

    public void carTirePressureChecked(String processId, String maintenanceId, String tireDescription) {
        TestProcess.carTirePressureChecked(messagePublisher, processId, maintenanceId, tireDescription);
    }

    public void completeRaceCarPostChecks(List<String> carNumbers) {
        TestProcess.completeRaceCarPostChecks(messagePublisher, carNumbers);
    }

    public void weatherAlertActivated(String weatherAlertSubject) {
        TestProcess.weatherAlertActivated(messagePublisher, weatherAlertSubject);
    }

    public void cancelRace(String processId, String raceId) {
        TestProcess.cancelRace(messagePublisher, processId, raceId);
    }

    public void reviewDocument(String processId, DocumentReviewDTO documentReviewDTO) {
        DocumentReviewProcess.reviewDocument(messagePublisher, processId, documentReviewDTO);
    }

    public void createDocumentVersion(String processId, NewDocumentVersionDTO newDocumentVersionDTO) {
        DocumentReviewProcess.createDocumentVersion(messagePublisher, processId, newDocumentVersionDTO);
    }

    public void reviewDocumentVersion(String processId, NewDocumentVersionReviewDTO newDocumentVersionReviewDTO) {
        DocumentReviewProcess.reviewDocumentVersion(messagePublisher, processId, newDocumentVersionReviewDTO);
    }
}
