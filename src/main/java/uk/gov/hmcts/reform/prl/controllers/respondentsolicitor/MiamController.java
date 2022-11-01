package uk.gov.hmcts.reform.prl.controllers.respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
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
    @Operation(description = "Callback for add case number submit event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestBody CallbackRequest callbackRequest
    ) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("handleAboutToStart: Callback for Respondent Solicitor - MIAM details  - Load What is MIAM?", callbackRequest);
        String collapsale1 = miamService.getCollapsableOfWhatIsMiamPlaceHolder();
        String collapsale2 = miamService.getCollapsableOfHelpMiamCostsExemptionsPlaceHolder();
        caseDataUpdated.put("whatIsMiamPlaceHolder", collapsale1);
        caseDataUpdated.put("helpMiamCostsExemptionsPlaceHolder", collapsale2);
        log.info("*****collapsale1****** - ", collapsale1);
        log.info("*****collapsale2****** - ", collapsale2);
        log.info("*****CaseDataUpdated****** - ", caseDataUpdated);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
