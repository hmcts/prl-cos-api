package uk.gov.hmcts.reform.prl.controllers.testingsupport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingRequest;
import uk.gov.hmcts.reform.prl.services.hearingmanagement.HearingManagementService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequestMapping("/hearing-support/testing")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hearing.hack.enabled", havingValue = "true")
public class HearingSupportController {
    private final HearingManagementService hearingManagementService;

    @PutMapping(path = "hearing-management-state-update/{caseState}", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Hack to trigger hearing event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public void caseStateUpdateByHearingManagement(@RequestBody HearingRequest hearingRequest,
                                                          @PathVariable("caseState") State caseState) throws Exception {
        log.info("Hack to trigger hearing event");
        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest,caseState);
    }

    @PutMapping(path = "hearing-management-state-update/caseState/PREPARE_FOR_HEARING_CONDUCT_HEARING", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Swagger invocation- ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public void valueCaseStateUpdateByHearingManagement(@RequestBody HearingRequest hearingRequest,
                                                   @PathVariable("caseState") String caseState) throws Exception {
        log.info("Hack to trigger hearing event with case state {}", caseState);
        State state = State.valueOf(caseState);
        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest,state);
    }
}
