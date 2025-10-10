package uk.gov.hmcts.reform.prl.controllers;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.CitizenSos;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.StmtOfServImplService;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassDateTimeService;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOS_CONFIRMATION_BODY_PREFIX;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOS_CONFIRMATION_HEADER;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SecurityRequirement(name = "Bearer Authentication")
public class StatementOfServiceController {

    private final AuthorisationService authorisationService;
    private final StmtOfServImplService stmtOfServImplService;
    private final ObjectMapper objectMapper;
    private final CafcassDateTimeService cafcassDateTimeService;

    @PostMapping(path = "/Statement-of-service-about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Statement of service about to start.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = uk.gov.hmcts.reform.ccd.client.model.CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse sosAboutToStart(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation))
            && Boolean.TRUE.equals(authorisationService.authoriseService(s2sToken))) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(stmtOfServImplService.retrieveRespondentsList(callbackRequest.getCaseDetails())).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/Statement-of-service-about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Statement of service about to start.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = uk.gov.hmcts.reform.ccd.client.model.CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse sosAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation))
            && Boolean.TRUE.equals(authorisationService.authoriseService(s2sToken))) {
            Map<String, Object> caseDataMap = stmtOfServImplService.handleSosAboutToSubmit(
                callbackRequest.getCaseDetails(),
                authorisation
            );
            cafcassDateTimeService.updateCafcassDateTime(callbackRequest);
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataMap).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/Statement-of-service-confirmation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Statement of service confirmation.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = uk.gov.hmcts.reform.ccd.client.model.CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity<SubmittedCallbackResponse> sosSubmitConfirmation(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation))
            && Boolean.TRUE.equals(authorisationService.authoriseService(s2sToken))) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            return ok(SubmittedCallbackResponse.builder().confirmationHeader(
                SOS_CONFIRMATION_HEADER).confirmationBody(
                C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication()) ? SOS_CONFIRMATION_BODY_PREFIX : null
            ).build());
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/{caseId}/{eventId}/save-statement-of-service-by-citizen", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Updating casedata with citizen sos")
    public ResponseEntity<Object> citizenSoaSubmit(
        @PathVariable("caseId") String caseId,
        @PathVariable("eventId") String eventId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CitizenSos sosObject
    ) {
        if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation))
            && Boolean.TRUE.equals(authorisationService.authoriseService(s2sToken))) {
            stmtOfServImplService.saveCitizenSos(caseId, eventId, authorisation, sosObject);
            return ResponseEntity.ok().build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
