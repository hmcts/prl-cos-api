package uk.gov.hmcts.reform.prl.controllers.restrictedcaseaccess;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.services.extendedcasedataservice.ExtendedCaseDataService;
import uk.gov.hmcts.reform.prl.services.restrictedcaseaccess.RestrictedCaseAccessService;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequestMapping("/restricted-case-access")
@RequiredArgsConstructor
public class RestrictedCaseAccessController {
    private final ExtendedCaseDataService caseDataService;
    private final RestrictedCaseAccessService restrictedCaseAccessService;

    @PostMapping(path = "/mark-as-restricted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as restricted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public uk.gov.hmcts.reform.prl.models.ccd.AboutToStartOrSubmitCallbackResponse markAsRestricted11(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        log.info("markAsRestricted7");
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("caseDataUpdated::" + caseDataUpdated);
        Map<String, Object> dataClassification
            = caseDataService.getDataClassification(String.valueOf(callbackRequest.getCaseDetails().getId()));
        log.info("dataClassification::" + dataClassification);
        return uk.gov.hmcts.reform.prl.models.ccd.AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .dataClassification(dataClassification)
            .securityClassification("RESTRICTED")
            .build();
    }

    @PostMapping(path = "/mark-as-restricted-workaround", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark case as restricted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public uk.gov.hmcts.reform.prl.models.ccd.AboutToStartOrSubmitCallbackResponse markAsRestrictedWorkaround(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        log.info("markAsRestricted7");
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("caseDataUpdated::" + caseDataUpdated);
        return uk.gov.hmcts.reform.prl.models.ccd.AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .build();
    }

    @PostMapping(path = "/submitted-restricted-confirmation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Load confirmation page for Restricted case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<SubmittedCallbackResponse> loadRestrictedCaseConfirmationPage(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @RequestBody CallbackRequest callbackRequest) {
        return restrictedCaseAccessService.restrictedCaseConfirmation();
    }

    @PostMapping(path = "/mark-as-public", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Mark the case as public")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse markCaseAsPublic(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("reasonsToRestrict", "");
        log.info("caseDataUpdated:", caseDataUpdated);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .build();
    }

    @PostMapping(path = "/submitted-public-confirmation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Load confirmation pagefor public cases")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<SubmittedCallbackResponse> loadPublicCaseConfirmationPage(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @RequestBody CallbackRequest callbackRequest) {
        return restrictedCaseAccessService.publicCaseConfirmation(callbackRequest);
    }
}

