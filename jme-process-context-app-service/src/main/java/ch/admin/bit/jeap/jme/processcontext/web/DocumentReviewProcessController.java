package ch.admin.bit.jeap.jme.processcontext.web;

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

@Tag(name = "DocumentProcess", description = "A simple non-rest interface to control a document review process tracked by the process context service")
@RestController
@RequestMapping("/api/documentprocess/")
@RequiredArgsConstructor
@Slf4j
class DocumentReviewProcessController {

    private final TestProcessService testProcessService;

    @PostMapping("{processId}/createProcess")
    @Operation(summary = "Create a new document review process", responses = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "200", description = "Process already exists")
    })
    public ResponseEntity<Void> create(@PathVariable("processId") String id, @RequestBody @Valid NewDocumentReviewProcessDTO newDocumentReviewProcessDTO) {
        if (testProcessService.findById(id).isPresent()) {
            log.info("TestProcess with processId {} already exists", id);
            return ResponseEntity.status(HttpStatus.OK).build();
        }

        testProcessService.createDocumentReviewProcess(id, newDocumentReviewProcessDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("{processId}/finishReview")
    @Operation(summary = "Finish the document review", responses = @ApiResponse(responseCode = "200", description = "Event produced successfully"))
    public void review(@PathVariable("processId") String id, @RequestBody @Valid DocumentReviewDTO documentReviewDTO) {
        testProcessService.reviewDocument(id, documentReviewDTO);
    }

    @PostMapping("{processId}/createDocumentVersion")
    @Operation(summary = "Create a new document version", responses = @ApiResponse(responseCode = "200", description = "Event produced successfully"))
    public void createDocumentVersion(@PathVariable("processId") String id, @RequestBody @Valid NewDocumentVersionDTO newDocumentVersionDTO) {
        testProcessService.createDocumentVersion(id, newDocumentVersionDTO);
    }

    @PostMapping("{processId}/createDocumentVersionReview")
    @Operation(summary = "Create a new document version review", responses = @ApiResponse(responseCode = "200", description = "Event produced successfully"))
    public void createDocumentVersionReview(@PathVariable("processId") String id, @RequestBody @Valid NewDocumentVersionReviewDTO newDocumentVersionReviewDTO) {
        testProcessService.reviewDocumentVersion(id, newDocumentVersionReviewDTO);
    }


}
