package uk.gov.hmcts.reform.prl.controllers.testingsupport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingRequest;
import uk.gov.hmcts.reform.prl.services.hearingmanagement.HearingManagementService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("/hearing-support/testing")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hearing.preview.bypass.enabled", havingValue = "true")
public class HearingSupportController {
    private final HearingManagementService hearingManagementService;

    @PutMapping(path = "prepare-for-hearing",
        consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Invocation for PREPARE_FOR_HEARING_CONDUCT_HEARING")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public void prepareForHearing(@RequestBody HearingRequest hearingRequest) {
        log.info("Trigger hearing event with state of PREPARE_FOR_HEARING_CONDUCT_HEARING");
        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest, State.PREPARE_FOR_HEARING_CONDUCT_HEARING);
    }

    @GetMapping(path = "is-enabled")
    @Operation(description = "To check whether end point is enabled")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity<Object> isEnabled() {
        log.info("Hearing support controller is enabled");
        return ok().build();
    }



}
