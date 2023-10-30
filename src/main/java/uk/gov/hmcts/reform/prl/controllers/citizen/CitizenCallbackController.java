package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenEmailService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
public class CitizenCallbackController extends AbstractCallbackController {
    private final AllTabServiceImpl allTabsService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    //private final ObjectMapper objectMapper;
    private final SystemUserService systemUserService;
    private final CitizenEmailService citizenEmailService;

    @Autowired
    protected CitizenCallbackController(ObjectMapper objectMapper,
                                        EventService eventPublisher,
                                        AllTabServiceImpl allTabsService,
                                        CoreCaseDataApi coreCaseDataApi,
                                        AuthTokenGenerator authTokenGenerator,
                                        //ObjectMapper objectMapper1,
                                        SystemUserService systemUserService,
                                        CitizenEmailService citizenEmailService) {
        super(objectMapper, eventPublisher);
        this.allTabsService = allTabsService;
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        //this.objectMapper = objectMapper1;
        this.systemUserService = systemUserService;
        this.citizenEmailService = citizenEmailService;
    }

    @PostMapping("/citizen-case-creation-callback/submitted")
    public void handleSubmitted(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
                                @RequestBody CallbackRequest callbackRequest) {

        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        String userToken = systemUserService.getSysUserToken();
        // setting supplementary data updates to enable global search
        String caseId = String.valueOf(caseData.getId());
        Map<String, Map<String, Map<String, Object>>> supplementaryData = new HashMap<>();
        supplementaryData.put(
            "supplementary_data_updates",
            Map.of("$set", Map.of("HMCTSServiceId", "ABA5"))
        );
        coreCaseDataApi.submitSupplementaryData(userToken, authTokenGenerator.generate(), caseId,
                                                supplementaryData
        );

        publishEvent(new CaseDataChanged(caseData));
    }

    @PostMapping(path = "/update-citizen-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to refresh the tabs")
    @SecurityRequirement(name = "Bearer Authentication")
    public void updateCitizenApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        allTabsService.updateAllTabsIncludingConfTab(caseData);
        citizenEmailService.sendCitizenCaseSubmissionEmail(authorisation, caseData);
    }

    @PostMapping(path = "/citizen-case-withdrawn-notification", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to send notifications on case withdrawn")
    @SecurityRequirement(name = "Bearer Authentication")
    public void sendNotificationsOnCaseWithdrawn(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) {
        log.info("sending email notification on case withdraw");
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        citizenEmailService.sendCitizenCaseWithdrawalEmail(authorisation, caseData);
    }
}
