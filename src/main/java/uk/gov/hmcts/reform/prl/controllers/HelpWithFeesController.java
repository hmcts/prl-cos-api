package uk.gov.hmcts.reform.prl.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.HelpWithFeesService;

import java.util.List;
import javax.ws.rs.NotFoundException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@RestController
@RequestMapping("/process-help-with-fees")
@Slf4j
@RequiredArgsConstructor
public class HelpWithFeesController {

    private final HelpWithFeesService helpWithFeesService;

    private final AuthorisationService authorisationService;

    @PostMapping(path = "/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "about to start callback for Process Urgent Help with Fees event.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestHeader("Authorization") @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return AboutToStartOrSubmitCallbackResponse.builder().data(helpWithFeesService.handleAboutToStart(
                callbackRequest.getCaseDetails())).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "about to start callback for Process Urgent Help with Fees event.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse checkForManagerApproval(
        @RequestHeader("Authorization") @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            List<String> errorList = helpWithFeesService.checkForManagerApproval(callbackRequest.getCaseDetails());
            if (isNotEmpty(errorList)) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errorList)
                    .build();
            }

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(callbackRequest.getCaseDetails().getData()).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "about to submit callback for Process Urgent Help with Fees event.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader("Authorization") @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return AboutToStartOrSubmitCallbackResponse.builder().data(helpWithFeesService
                .setCaseStatus(callbackRequest, authorisation)).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "submitted callback for Process Urgent Help with Fees event.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<SubmittedCallbackResponse> handleSubmitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws NotFoundException {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return helpWithFeesService
                .handleSubmitted();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/populate-hwf-dynamic-data", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "populate the dynamic data for HWF.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse populateHwfDynamicData(
        @RequestHeader("Authorization") @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return AboutToStartOrSubmitCallbackResponse.builder().data(helpWithFeesService
                .populateHwfDynamicData(callbackRequest.getCaseDetails())).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
