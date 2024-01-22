package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchdarkly.shaded.com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.RespondentAllegationOfHarmService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@RestController
@RequestMapping("/respondent-aoh")
@Slf4j
public class RespondentAllegationOfHarmController extends AbstractCallbackController {

    private final RespondentAllegationOfHarmService respondentAllegationOfHarmService;
    private final AuthorisationService authorisationService;
    private final C100RespondentSolicitorService respondentSolicitorService;


    @Autowired
    public RespondentAllegationOfHarmController(ObjectMapper objectMapper, EventService eventPublisher,
                                                RespondentAllegationOfHarmService respondentAllegationOfHarmService,
                                                AuthorisationService authorisationService,
                                                C100RespondentSolicitorService respondentSolicitorService) {
        super(objectMapper, eventPublisher);
        this.respondentAllegationOfHarmService = respondentAllegationOfHarmService;
        this.authorisationService = authorisationService;
        this.respondentSolicitorService = respondentSolicitorService;
    }

    @PostMapping(path = "/pre-populate-child-data", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for Aoh pre populate child data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
            @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
            @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            log.info("pre-populate-child-data: Aoh pre populate child data");
            List<String> errorList = new ArrayList<>();
            CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
            Map<String, Object> caseDataUpdated = respondentSolicitorService.populateAboutToStartCaseData(
                    callbackRequest);
            respondentAllegationOfHarmService.prePopulatedChildData(
                            caseData,caseDataUpdated);
            log.info("caseData {}",new Gson().toJson(AboutToStartOrSubmitCallbackResponse
                    .builder()
                    .data(caseDataUpdated).errors(errorList).build()));
            return AboutToStartOrSubmitCallbackResponse
                    .builder()
                    .data(caseDataUpdated).errors(errorList).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
