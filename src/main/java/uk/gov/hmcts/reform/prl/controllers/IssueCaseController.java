package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.rpa.mappers.C100JsonMapper;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.ExampleService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.pin.C100CaseInviteService;
import uk.gov.hmcts.reform.prl.services.pin.FL401CaseInviteService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@Slf4j
@RestController
@RequiredArgsConstructor
public class IssueCaseController {

    private final OrganisationService organisationService;
    private final CaseWorkerEmailService caseWorkerEmailService;
    private final ObjectMapper objectMapper;
    private final AllTabServiceImpl allTabsService;
    private final DocumentGenService documentGenService;
    private final SendgridService sendgridService;
    private final C100JsonMapper c100JsonMapper;
    private final C100CaseInviteService c100CaseInviteService;
    private final FL401CaseInviteService fl401CaseInviteService;
    private final LaunchDarklyClient launchDarklyClient;

    @PostMapping(path = "/issue-and-send-to-local-court", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to Issue and send to local court")
    public AboutToStartOrSubmitCallbackResponse issueAndSendToLocalCourt(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) throws Exception {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        if (YesOrNo.No.equals(caseData.getConsentOrder())) {
            requireNonNull(caseData);
            sendgridService.sendEmail(c100JsonMapper.map(caseData));
        }
        caseData = caseData.toBuilder().issueDate(LocalDate.now()).build();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        // Generate All Docs and set to casedataupdated.
        caseDataUpdated.putAll(documentGenService.generateDocuments(authorisation, caseData));

        // Refreshing the page in the same event. Hence no external event call needed.
        // Getting the tab fields and add it to the casedetails..
        Map<String, Object> allTabsFields = allTabsService.getAllTabsFields(caseData);

        caseDataUpdated.putAll(allTabsFields);
        caseDataUpdated.put("issueDate", caseData.getIssueDate());

        try {
            caseWorkerEmailService.sendEmailToCourtAdmin(callbackRequest.getCaseDetails());
        } catch (Exception ex) {
            log.info("Email notification could not be sent due to following {}", ex.getMessage());
        }

        if (launchDarklyClient.isFeatureEnabled("generate-pin")) {

            log.info("Generating and sending PIN to respondents");

            if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                caseData = c100CaseInviteService.generateAndSendRespondentCaseInvite(caseData);
            } else {
                caseData = fl401CaseInviteService.generateAndSendRespondentCaseInvite(caseData);
            }
            caseDataUpdated.putAll(caseData.toMap(objectMapper));
        }

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

}
