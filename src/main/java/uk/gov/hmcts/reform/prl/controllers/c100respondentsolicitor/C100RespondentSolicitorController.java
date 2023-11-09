package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@RestController
@RequestMapping("/respondent-solicitor")
@Slf4j
public class C100RespondentSolicitorController extends AbstractCallbackController {
    private final C100RespondentSolicitorService respondentSolicitorService;
    private final AuthorisationService authorisationService;

    @Autowired
    public C100RespondentSolicitorController(ObjectMapper objectMapper, EventService eventPublisher,
                                             C100RespondentSolicitorService respondentSolicitorService,
                                             AuthorisationService authorisationService) {
        super(objectMapper, eventPublisher);
        this.respondentSolicitorService = respondentSolicitorService;
        this.authorisationService = authorisationService;
    }

    @PostMapping(path = "/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Respondent Solicitor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            log.info("handleAboutToStart: Callback for Respondent Solicitor - Load the case data");
            List<String> errorList = new ArrayList<>();
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(respondentSolicitorService.populateAboutToStartCaseData(
                    callbackRequest
                )).errors(errorList).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Respondent Solicitor about to submit event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        log.info("handleAboutToSubmit: Callback for about-to-submit");
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            List<String> errorList = new ArrayList<>();
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(respondentSolicitorService.populateAboutToSubmitCaseData(
                    callbackRequest))
                .errors(errorList).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/keep-details-private-list", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to send FL401 application notification. ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application Submitted."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public CallbackResponse generateConfidentialityDynamicSelectionDisplay(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            Map<String, Object> updatedCaseData = respondentSolicitorService.generateConfidentialityDynamicSelectionDisplay(
                callbackRequest);
            CaseData caseData = objectMapper.convertValue(updatedCaseData, CaseData.class);
            return CallbackResponse.builder()
                .data(caseData.toBuilder()
                          .id(callbackRequest.getCaseDetails().getId())
                          .build())
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/generate-c7response-document", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to generate and store document")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse generateC7ResponseDraftDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody @Parameter(name = "CaseData") uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) throws Exception {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(respondentSolicitorService.generateDraftDocumentsForRespondent(
                    callbackRequest,
                    authorisation))
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/about-to-start-response-validation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Respondent Solicitor - validate response events before submit")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse validateActiveRespondentResponseBeforeStart(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            List<String> errorList = new ArrayList<>();
            log.info("validateTheResponseBeforeSubmit: Callback for Respondent Solicitor - validate response");
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(respondentSolicitorService.validateActiveRespondentResponse(
                    callbackRequest,
                    errorList,
                    authorisation))
                .errors(errorList)
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/submit-c7-response", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Respondent Solicitor - update c7 response after submission")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse updateC7ResponseSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            List<String> errorList = new ArrayList<>();
            log.info("validateTheResponseBeforeSubmit: Callback for Respondent Solicitor - validate response");
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(respondentSolicitorService.submitC7ResponseForActiveRespondent(
                    authorisation,
                    callbackRequest
                ))
                .errors(errorList)
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Submitted c7 response")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<SubmittedCallbackResponse> submittedC7Response(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        publishEvent(new CaseDataChanged(caseData));
        return ok(respondentSolicitorService.submittedC7Response(caseData));
    }
}
