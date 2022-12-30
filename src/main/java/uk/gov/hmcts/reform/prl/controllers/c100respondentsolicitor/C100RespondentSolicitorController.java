package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentSolicitorMiamService;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequestMapping("/respondent-solicitor")
@Slf4j
public class C100RespondentSolicitorController {

    @Autowired
    private RespondentSolicitorMiamService miamService;

    @Autowired
    C100RespondentSolicitorService respondentSolicitorService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(path = "/about-to-start-miam", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Respondent Solicitor - MIAM details  - Load What is MIAM?")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToStartMiam(
        @RequestBody CallbackRequest callbackRequest
    ) {

        log.info("handleAboutToStart: Callback for Respondent Solicitor - MIAM details  - Load What is MIAM?");
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("whatIsMiamPlaceHolder", miamService.getCollapsableOfWhatIsMiamPlaceHolder());
        caseDataUpdated.put(
            "helpMiamCostsExemptionsPlaceHolder",
            miamService.getCollapsableOfHelpMiamCostsExemptionsPlaceHolder()
        );
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/mid-event-miam", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Respondent Solicitor - MIAM details - handleMidEvent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleMidEventMiam(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        log.info("handleMidEvent: Callback for Respondent Solicitor - MIAM details");
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("in C100RespondentSolicitorController - handleMidEvent - caseDataUpdated {}", caseDataUpdated);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/about-to-submit-miam", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Respondent Solicitor - MIAM details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitMiam(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        log.info("handleAboutToSubmit: Callback for Respondent Solicitor - MIAM details");
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        log.info("in C100RespondentSolicitorController - handleAboutToSubmit - caseDataUpdated {}", updatedCaseData);
        return AboutToStartOrSubmitCallbackResponse.builder().data(updatedCaseData).build();
    }

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
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(respondentSolicitorService.populateAboutToStartCaseData(
                callbackRequest,
                authorisation
            )).build();
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
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(respondentSolicitorService.populateAboutToSubmitCaseData(
                callbackRequest,
                authorisation
            )).build();
    }

    @PostMapping(path = "/populate-solicitor-respondent-list", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate the header")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse populateSolicitorRespondentList(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {
        log.info("populateSolicitorRespondentList: Callback for getting the respondent listing");
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(respondentSolicitorService.populateSolicitorRespondentList(callbackRequest, authorisation))
            .build();
    }

    @PostMapping(path = "/respondent-selection-about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Respondent Solicitor - submit active respondent selection")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleActiveRespondentSelection(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        log.info("handleActiveRespondentSelection: Callback for Respondent Solicitor - handle select respondent");
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(respondentSolicitorService.updateActiveRespondentSelectionBySolicitor(
                callbackRequest,
                authorisation
            )).build();
    }
}
