package uk.gov.hmcts.reform.prl.controllers.respondentsolicitor;

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
import uk.gov.hmcts.reform.prl.models.complextypes.IsMiamAttended;
import uk.gov.hmcts.reform.prl.models.complextypes.WillYouAttendMiam;
import uk.gov.hmcts.reform.prl.services.RespondentSolicitorMiamService;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequestMapping("/respondent-solicitor-miam")
@Slf4j
public class MiamController {

    @Autowired
    private RespondentSolicitorMiamService miamService;

    @Autowired
    private ObjectMapper objectMapper;


    @PostMapping(path = "/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Respondent Solicitor - MIAM details  - Load What is MIAM?")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestBody CallbackRequest callbackRequest
    ) {

        log.info("handleAboutToStart: Callback for Respondent Solicitor - MIAM details  - Load What is MIAM?");
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("whatIsMiamPlaceHolder", IsMiamAttended.builder()
            .whatIsMiamPlaceHolder(miamService.getCollapsableOfWhatIsMiamPlaceHolder())
            .build());
        caseDataUpdated.put("helpMiamCostsExemptionsPlaceHolder", WillYouAttendMiam.builder()
            .helpMiamCostsExemptionsPlaceHolder(miamService.getCollapsableOfHelpMiamCostsExemptionsPlaceHolder())
            .build());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }


    @PostMapping(path = "/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Respondent Solicitor - MIAM details - handleMidEvent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        log.info("handleMidEvent: Callback for Respondent Solicitor - MIAM details");
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("in MiamController - handleMidEvent - caseDataUpdated {}", caseDataUpdated);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }


    @PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Respondent Solicitor - MIAM details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws Exception {

        log.info("handleAboutToSubmit: Callback for Respondent Solicitor - MIAM details");
        Map<String,Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        log.info("in MiamController - handleAboutToSubmit - caseDataUpdated {}", updatedCaseData);
        return AboutToStartOrSubmitCallbackResponse.builder().data(updatedCaseData).build();
    }
}
