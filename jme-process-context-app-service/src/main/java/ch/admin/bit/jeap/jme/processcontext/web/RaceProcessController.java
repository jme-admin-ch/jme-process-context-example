package ch.admin.bit.jeap.jme.processcontext.web;

import ch.admin.bit.jeap.jme.processcontext.db.StatisticService;
import ch.admin.bit.jeap.jme.processcontext.domain.ProcessCreationType;
import ch.admin.bit.jeap.jme.processcontext.domain.TestProcess;
import ch.admin.bit.jeap.jme.processcontext.domain.TestProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "RaceProcess", description = "A simple non-rest interface to control a race process tracked by the process context service")
@RestController
@RequestMapping("/api/raceprocess/")
@RequiredArgsConstructor
@Slf4j
class RaceProcessController {

    private final TestProcessService testProcessService;
    private final StatisticService statisticService;

    @GetMapping("{processId}")
    @Operation(summary = "Get an existing test process", responses = @ApiResponse(responseCode = "200", description = "Found"))
    public TestProcess get(@PathVariable("processId") String id) {
        return testProcessService.get(id);
    }

    @PostMapping("{processId}/createProcess")
    @Operation(summary = "Create a new test process", responses = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "200", description = "Process already exists")
    })
    public ResponseEntity<Void> create(@PathVariable("processId") String id, @RequestParam(name = "processCreationType", defaultValue = "REST") ProcessCreationType processCreationType, @RequestBody @Valid NewProcessDTO newProcessDTO) {
        if (testProcessService.findById(id).isPresent()) {
            log.info("TestProcess with processId {} already exists", id);
            return ResponseEntity.status(HttpStatus.OK).build();
        }

        testProcessService.create(id, processCreationType, newProcessDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("{processId}/raceStarted")
    @Operation(summary = "Produce RaceStarted event & plan control points", responses = @ApiResponse(responseCode = "200", description = "Events produced successfully"))
    public void raceStarted(@PathVariable("processId") String processId, @RequestParam("weatherAlertSubject") String weatherAlertSubject) {
        testProcessService.raceStarted(processId, weatherAlertSubject);
    }

    @PostMapping("{processId}/raceControlpointPassed")
    @Operation(summary = "Simulate RaceControlpointPassedEvent(s) for a control point", responses = @ApiResponse(responseCode = "200", description = "Event(s) produced successfully"))
    public void raceControlpointPassed(@PathVariable("processId") String processId,
                                       @RequestParam("controlPoint") String controlPoint) {
        testProcessService.raceControlpointPassed(processId, controlPoint);
    }

    @PostMapping("{processId}/raceDestinationReached")
    @Operation(summary = "Produce CarExited event", responses = @ApiResponse(responseCode = "200", description = "Event produced successfully"))
    public void carExited(@PathVariable("processId") String processId) {
        testProcessService.get(processId).raceDestinationReached();
    }

    @PostMapping("{processId}/raceValidated")
    @Operation(summary = "Completed validateRace task using JmeRaceValidatedEvent", responses = @ApiResponse(responseCode = "200", description = "Event produced successfully"))
    public void raceValidated(@PathVariable("processId") String processId) {
        testProcessService.get(processId).raceValidated();
    }

    @PostMapping("{processId}/mobileCheckpointPassed")
    @Operation(summary = "Produce JmeRaceMobileCheckpointPassedEvent", responses = @ApiResponse(responseCode = "200", description = "Event produced successfully"))
    public void mobileCheckpointPassed(@PathVariable("processId") String processId,
                                       @RequestParam("checkpoint") String checkpoint,
                                       @RequestParam("state") String state,
                                       @RequestParam("taskId") String taskId) {
        testProcessService.mobileCheckpointPassed(processId, checkpoint, state, taskId);
    }

    @PostMapping("{processId}/raceCarPostChecksPlanned")
    @Operation(summary = "Plan race car post checks.", responses = @ApiResponse(responseCode = "200", description = "Planned successfully"))
    public void raceCarPostChecksPlanned(@PathVariable("processId") String processId) {
        testProcessService.planRaceCarPostChecks(processId);
    }

    @PostMapping("{processId}/raceCarPostChecksSkipped")
    @Operation(summary = "Skip race car post checks.", responses = @ApiResponse(responseCode = "200", description = "Skipped successfully"))
    public void raceCarPostChecksSkipped(@PathVariable("processId") String processId) {
        testProcessService.get(processId).skipRaceCarPostChecks(processId);
    }


    @PostMapping("{processId}/carMaintenanceRequired")
    @Operation(summary = "Produce JmeRaceCarMaintenanceRequiredEvent", responses = @ApiResponse(responseCode = "200", description = "Event produced successfully"))
    public void carMaintenanceRequired(@PathVariable("processId") String processId, @RequestParam("maintenanceId") String maintenanceId) {
        testProcessService.carMaintenanceRequired(processId, maintenanceId);
    }

    @PostMapping("{processId}/carRefuellingCompleted")
    @Operation(summary = "Produce JmeRaceCarRefuellingCompletedEvent", responses = @ApiResponse(responseCode = "200", description = "Event produced successfully"))
    public void carRefuellingCompleted(@PathVariable("processId") String processId) {
        testProcessService.carRefuellingCompleted(processId);
    }

    @PostMapping("{processId}/carTirePressureChecked")
    @Operation(summary = "Produce JmeRaceCarTirePressureCheckedEvent", responses = @ApiResponse(responseCode = "200", description = "Event produced successfully"))
    public void carTirePressureChecked(@PathVariable("processId") String processId, @RequestParam("tireDescription") String tireDescription, @RequestParam("maintenanceId") String maintenanceId) {
        testProcessService.carTirePressureChecked(processId, maintenanceId, tireDescription);
    }

    @PostMapping("raceCarPostChecksCompleted")
    @Operation(summary = "Produce JmeRaceCarPostChecksCompletedEvent for the cars checked.", responses = @ApiResponse(responseCode = "200", description = "Event produced successfully"))
    public void raceCarPostChecksCompleted(@RequestBody RaceCarPostChecksCompletedDto raceCarPostChecksCompletedDto) {
        testProcessService.completeRaceCarPostChecks(raceCarPostChecksCompletedDto.getCarNumbers());
    }

    @PostMapping("weatherAlertActivated")
    @Operation(summary = "Produce JmeRaceWeatherAlertActivatedEvent", responses = @ApiResponse(responseCode = "200", description = "Event produced successfully"))
    public void weatherAlertActivated(@RequestParam("weatherAlertSubject") String weatherAlertSubject) {
        testProcessService.weatherAlertActivated(weatherAlertSubject);
    }

    @PostMapping("{processId}/cancelRace")
    @Operation(summary = "Produce JmeCancelRaceCommand", responses = @ApiResponse(responseCode = "200", description = "Command produced successfully"))
    public void cancelRace(@PathVariable("processId") String processId, @RequestParam("raceId") String raceId) {
        testProcessService.cancelRace(processId, raceId);
    }

    @PostMapping("{processId}/objectsOnTheRoad")
    @Operation(summary = "Produce JmeRaceObjectsOnTheRoadSpottedEvent", responses = @ApiResponse(responseCode = "200", description = "Event produced successfully"))
    public void objectsOnTheRoadSpotted(@PathVariable("processId") String processId, @RequestParam(name = "triggerSafetyCar", defaultValue = "false") String triggerSafetyCar) {
        testProcessService.objectsOnTheRoadSpotted(processId, Boolean.parseBoolean(triggerSafetyCar));
    }

    @GetMapping("countInstances")
    public StatisticDto countInstances() {
        return statisticService.countRows();
    }
}
