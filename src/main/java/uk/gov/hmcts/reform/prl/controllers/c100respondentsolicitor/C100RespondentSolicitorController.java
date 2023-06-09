package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequestMapping("/respondent-solicitor")
@Slf4j
public class C100RespondentSolicitorController {

    @Autowired
    C100RespondentSolicitorService respondentSolicitorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    ConfidentialDetailsMapper confidentialDetailsMapper;

    @PostMapping(path = "/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Respondent Solicitor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        log.info("handleAboutToStart: Callback for Respondent Solicitor - Load the case data");
        List<String> errorList = new ArrayList<>();
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(respondentSolicitorService.populateAboutToStartCaseData(
                callbackRequest
            )).errors(errorList).build();
    }

    @PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Respondent Solicitor about to submit event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        log.info("handleAboutToSubmit: Callback for about-to-submit");
        List<String> errorList = new ArrayList<>();
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(respondentSolicitorService.populateAboutToSubmitCaseData(
                callbackRequest
            ))
            .errors(errorList).build();
    }

    @PostMapping(path = "/keep-details-private-list", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to send FL401 application notification. ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application Submitted."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public CallbackResponse generateConfidentialityDynamicSelectionDisplay(
        @RequestBody CallbackRequest callbackRequest) {

        Map<String, Object> updatedCaseData = respondentSolicitorService.generateConfidentialityDynamicSelectionDisplay(callbackRequest);
        CaseData caseData = objectMapper.convertValue(updatedCaseData, CaseData.class);
        caseData = confidentialDetailsMapper.mapConfidentialData(caseData, true);
        return CallbackResponse.builder()
            .data(caseData.toBuilder()
                      .id(callbackRequest.getCaseDetails().getId())
                      .build())
            .build();
    }

    @PostMapping(path = "/generate-c7response-document", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to generate and store document")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse generateC7ResponseDraftDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody @Parameter(name = "CaseData") uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) throws Exception {

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(respondentSolicitorService.generateDraftDocumentsForRespondent(callbackRequest, authorisation))
            .build();
    }

    @PostMapping(path = "/about-to-start-response-validation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Respondent Solicitor - validate response events before submit")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse validateActiveRespondentResponseBeforeStart(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

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
    }

    @PostMapping(path = "/submit-c7-response", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Respondent Solicitor - update c7 response after submission")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse updateC7ResponseSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        List<String> errorList = new ArrayList<>();
        log.info("validateTheResponseBeforeSubmit: Callback for Respondent Solicitor - validate response");
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(respondentSolicitorService.submitC7ResponseForActiveRespondent(
                callbackRequest,
                authorisation,
                errorList))
            .errors(errorList)
            .build();
    }
}
