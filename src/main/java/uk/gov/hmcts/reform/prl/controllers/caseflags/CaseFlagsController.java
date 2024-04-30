package uk.gov.hmcts.reform.prl.controllers.caseflags;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsWaService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/caseflags")
public class CaseFlagsController {
    private final AuthorisationService authorisationService;
    private final CaseFlagsWaService caseFlagsWaService;
    private final ObjectMapper objectMapper;

    @PostMapping(path = "/setup-wa-task", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to validate case creator to decide on the WA task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public void setUpWaTaskForCaseFlags(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            try {
                log.info("setUpWaTaskForCaseFlags in case controller CaseDetails start json after ===>"
                             + objectMapper.writeValueAsString(callbackRequest.getCaseDetails()));
            } catch (JsonProcessingException e) {
                log.info("error");
            }

            try {
                log.info("setUpWaTaskForCaseFlags in case controller CaseDetails start json before ===>"
                             + objectMapper.writeValueAsString(callbackRequest.getCaseDetailsBefore()));
            } catch (JsonProcessingException e) {
                log.info("error");
            }
            caseFlagsWaService.setUpWaTaskForCaseFlags(authorisation,
                                                       String.valueOf(callbackRequest.getCaseDetails().getId())
            );
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/setup-wa-task-1", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to validate case creator to decide on the WA task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public void setUpWaTaskForCaseFlags1(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            try {
                log.info("setUpWaTaskForCaseFlags in case controller CaseDetails start json after ===>"
                             + objectMapper.writeValueAsString(callbackRequest.getCaseDetails()));
            } catch (JsonProcessingException e) {
                log.info("error");
            }

            try {
                log.info("setUpWaTaskForCaseFlags in case controller CaseDetails start json before ===>"
                             + objectMapper.writeValueAsString(callbackRequest.getCaseDetailsBefore()));
            } catch (JsonProcessingException e) {
                log.info("error");
            }
            caseFlagsWaService.setUpWaTaskForCaseFlagsCopy(authorisation,
                                                       callbackRequest
            );
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/setup-wa-task-2", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to validate case creator to decide on the WA task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public void setUpWaTaskForCaseFlags2(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            try {
                log.info("setUpWaTaskForCaseFlags in case controller CaseDetails start json after ===>"
                             + objectMapper.writeValueAsString(callbackRequest.getCaseDetails()));
            } catch (JsonProcessingException e) {
                log.info("error");
            }

            try {
                log.info("setUpWaTaskForCaseFlags in case controller CaseDetails start json before ===>"
                             + objectMapper.writeValueAsString(callbackRequest.getCaseDetailsBefore()));
            } catch (JsonProcessingException e) {
                log.info("error");
            }
            caseFlagsWaService.setUpWaTaskForCaseFlagsEventHandler(authorisation,
                                                           callbackRequest
            );
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
