package ch.admin.bit.jeap.jme.processcontext.web;

import ch.admin.bit.jeap.jme.processcontext.domain.ProcessContextClient;
import ch.admin.bit.jeap.processcontext.archive.processsnapshot.v2.ProcessSnapshot;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/snapshot/")
@Tag(name = "ProcessSnapshot", description = "API for viewing process snapshots")
@Slf4j
@RequiredArgsConstructor
class SnapshotController {

    private final ProcessContextClient processContextClient;

    @GetMapping("{originProcessId}/{version}")
    @Operation(summary = "Read a process snapshot version", responses = {
            @ApiResponse(responseCode = "200", description = "Process snapshot version found"),
            @ApiResponse(responseCode = "404", description = "Process snapshot version not found")
    })
    ResponseEntity<String> getSnapshot(@PathVariable("originProcessId") String originProcessId, @PathVariable("version") int version) {
        Optional<ProcessSnapshot> processSnapshotOptional = processContextClient.getProcessSnapshot(originProcessId, version);
        if (processSnapshotOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(processSnapshotOptional.get().toString());
        }
    }

}
