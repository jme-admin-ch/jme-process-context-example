package ch.admin.bit.jeap.jme.processcontext.web.perftest;

import ch.admin.bit.jeap.jme.processcontext.perftest.PerfTestService;
import ch.admin.bit.jeap.jme.processcontext.perftest.TestScenarioType;
import ch.admin.bit.jeap.jme.processcontext.perftest.scenario.HighMessageCountScenario;
import ch.admin.bit.jeap.jme.processcontext.perftest.scenario.ProcessRelationsScenario;
import ch.admin.bit.jeap.jme.processcontext.perftest.scenario.SimpleProcessScenario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "PerfTest", description = "Performance test driver for the Process Context Service")
@RestController
@RequestMapping("/api/perftests")
@RequiredArgsConstructor
@Slf4j
class PerfTestController {

    private final PerfTestService perfTestService;

    @PostMapping("/scenarios/highMessageCount")
    @Operation(summary = "Start a new HighMessageCountScenario performance test run",
            responses = {
                    @ApiResponse(responseCode = "202", description = "Test run accepted and started"),
                    @ApiResponse(responseCode = "400", description = "Invalid request")
            })
    public ResponseEntity<TestRunDTO> startHighMessageCountTest(@RequestParam(value = "processCount", defaultValue = "100") int processCount,
                                                                @RequestParam(value = "tasksPerProcessCount", defaultValue = "100") int tasksPerProcessCount,
                                                                @RequestParam(value = "messagePerProcessCount", defaultValue = "100000") int messagePerProcessCount,
                                                                @RequestParam(value = "warmUpProcessCount", defaultValue = "5") int warmUpProcessCount,
                                                                @RequestParam(value = "durationMinutes", defaultValue = "0") int durationMinutes,
                                                                @RequestParam(value = "timeoutMinutes", defaultValue = "10") int timeoutMinutes,
                                                                @RequestParam(value = "clearDatabase", defaultValue = "true") boolean clearDatabase) {
        var scenarioType = TestScenarioType.HIGH_MESSAGE_COUNT;
        Map<String, Object> parameters = Map.of(
                HighMessageCountScenario.TASKS_PER_PROCESS_COUNT, tasksPerProcessCount,
                HighMessageCountScenario.MESSAGE_PER_PROCESS_COUNT, messagePerProcessCount,
                HighMessageCountScenario.PROCESS_COUNT, processCount,
                HighMessageCountScenario.WARM_UP_PROCESS_COUNT, warmUpProcessCount,
                HighMessageCountScenario.DURATION_MINUTES, durationMinutes);
        return startTestRun(scenarioType, parameters, timeoutMinutes, clearDatabase);
    }

    @PostMapping("/scenarios/simpleProcess")
    @Operation(summary = "Start a new SimpleProcessScenario performance test run",
            responses = {
                    @ApiResponse(responseCode = "202", description = "Test run accepted and started"),
                    @ApiResponse(responseCode = "400", description = "Invalid request")
            })
    public ResponseEntity<TestRunDTO> startSimpleProcessTest(@RequestParam(value = "processCount", defaultValue = "100") int processCount,
                                                                @RequestParam(value = "warmUpProcessCount", defaultValue = "5") int warmUpProcessCount,
                                                                @RequestParam(value = "durationMinutes", defaultValue = "0") int durationMinutes,
                                                                @RequestParam(value = "timeoutMinutes", defaultValue = "10") int timeoutMinutes,
                                                                @RequestParam(value = "clearDatabase", defaultValue = "true") boolean clearDatabase) {
        var scenarioType = TestScenarioType.SIMPLE_PROCESS;
        Map<String, Object> parameters = Map.of(
                SimpleProcessScenario.PROCESS_COUNT, processCount,
                SimpleProcessScenario.WARM_UP_PROCESS_COUNT, warmUpProcessCount,
                SimpleProcessScenario.DURATION_MINUTES, durationMinutes);
        return startTestRun(scenarioType, parameters, timeoutMinutes, clearDatabase);
    }

    @PostMapping("/scenarios/processRelations")
    @Operation(summary = "Start a new ProcessRelationsScenario performance test run",
            responses = {
                    @ApiResponse(responseCode = "202", description = "Test run accepted and started"),
                    @ApiResponse(responseCode = "400", description = "Invalid request")
            })
    public ResponseEntity<TestRunDTO> startProcessRelationsTest(@RequestParam(value = "processCount", defaultValue = "100") int processCount,
                                                                @RequestParam(value = "warmUpProcessCount", defaultValue = "5") int warmUpProcessCount,
                                                                @RequestParam(value = "durationMinutes", defaultValue = "0") int durationMinutes,
                                                                @RequestParam(value = "timeoutMinutes", defaultValue = "10") int timeoutMinutes,
                                                                @RequestParam(value = "clearDatabase", defaultValue = "true") boolean clearDatabase) {
        var scenarioType = TestScenarioType.PROCESS_RELATIONS;
        Map<String, Object> parameters = Map.of(
                ProcessRelationsScenario.PROCESS_COUNT, processCount,
                ProcessRelationsScenario.WARM_UP_PROCESS_COUNT, warmUpProcessCount,
                ProcessRelationsScenario.DURATION_MINUTES, durationMinutes);
        return startTestRun(scenarioType, parameters, timeoutMinutes, clearDatabase);
    }

    private ResponseEntity<TestRunDTO> startTestRun(TestScenarioType scenarioType, Map<String, Object> parameters,
                                                    int timeoutMinutes, boolean clearDatabase) {
        var testRunId = UUID.randomUUID();
        var timeout = Duration.ofMinutes(timeoutMinutes);
        log.info("Starting perf test using scenario {} with ID {}", scenarioType, testRunId);
        perfTestService.startTestAsync(testRunId, scenarioType, parameters, timeout, clearDatabase);
        var dto = new TestRunDTO(testRunId, "/api/perftests/%s/report".formatted(testRunId), "/api/perftests/%s".formatted(testRunId));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(dto);
    }

    @GetMapping
    @Operation(summary = "List all test runs with their status",
            responses = @ApiResponse(responseCode = "200", description = "List of test runs"))
    public List<TestRunStatusDTO> listTestRuns() {
        return perfTestService.getAllTestRuns().stream()
                .map(TestRunStatusDTO::fromTestRun)
                .toList();
    }

    @DeleteMapping("{testRunId}")
    @Operation(summary = "Cancel a running performance test",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Test run cancelled"),
                    @ApiResponse(responseCode = "404", description = "Test run not found or already finished")
            })
    public ResponseEntity<Void> cancelTestRun(@PathVariable("testRunId") UUID testRunId) {
        boolean cancelled = perfTestService.cancelTestRun(testRunId);
        return cancelled ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("latest")
    @Operation(summary = "Get the status of the latest performance test run",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Test run found"),
                    @ApiResponse(responseCode = "404", description = "Test run not found")
            })
    public ResponseEntity<TestRunStatusDTO> getLatestTestStatus() {
        return perfTestService.getLatestTestRunId().map(this::getTestStatus)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("{testRunId}")
    @Operation(summary = "Get the status of a performance test run",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Test run found"),
                    @ApiResponse(responseCode = "404", description = "Test run not found")
            })
    public ResponseEntity<TestRunStatusDTO> getTestStatus(@PathVariable("testRunId") UUID testRunId) {
        return perfTestService.getTestRun(testRunId)
                .map(TestRunStatusDTO::fromTestRun)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(path = "latest/report", produces = "text/html")
    @Operation(summary = "Get the test report for the latest performance test run",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Test run found"),
                    @ApiResponse(responseCode = "404", description = "Test run not found")
            })
    public ResponseEntity<String> getLatestTestReport() {
        return perfTestService.getLatestTestRunId().map(this::getTestReport)
                .orElse(ResponseEntity.notFound().build());

    }

    @GetMapping(path = "{testRunId}/report", produces = "text/html")
    @Operation(summary = "Get the test report for a performance test run",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Test run found"),
                    @ApiResponse(responseCode = "404", description = "Test run not found")
            })
    public ResponseEntity<String> getTestReport(@PathVariable("testRunId") UUID testRunId) {
        return perfTestService.getTestReport(testRunId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
