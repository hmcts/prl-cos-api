package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.HearingDetailsRequest;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.HearingDetailsEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.*;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class HearingsNotificationController {

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CaseService caseService;

    @Autowired
    private IdamClient idamClient;

    @Value("${citizen.url}")
    private String hearingDetailsUrl;

    @PostMapping(path = "/{caseId}/{eventId}/{partyName}/hearing-details-notification", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Send a notification to the parties about the hearing date.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification Sent"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    public String sendHearingNotification(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                          @RequestHeader("serviceAuthorization") String s2sToken,
                                          @ModelAttribute HearingDetailsRequest hearingDetailsRequest) throws Exception {

        String caseId = hearingDetailsRequest.getCaseId();
        CaseDetails caseDetails = coreCaseDataApi.getCase(authorisation, s2sToken, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        String eventId = hearingDetailsRequest.getEventId();
        String newState;
        caseData = caseData.toBuilder()
            .state(State.CASE_HEARINGS)
            .build();

        StartEventResponse startEventResponse =
            coreCaseDataApi.startEventForCitizen(
                authorisation,
                s2sToken,
                idamClient.getUserInfo(authorisation).getUid(),
                JURISDICTION,
                CASE_TYPE,
                caseId,
                eventId
            );

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(caseData).build();

        coreCaseDataApi.submitEventForCitizen(
            authorisation,
            s2sToken,
            idamClient.getUserInfo(authorisation).getUid(),
            JURISDICTION,
            CASE_TYPE,
            caseId,
            true,
            caseDataContent
        );
        sendHearingDetailsEmailToCitizen(hearingDetailsRequest, caseData);

        return "SUCCESS";
    }

    private void sendHearingDetailsEmailToCitizen(HearingDetailsRequest hearingDetailsRequest, CaseData caseData) {

        emailService.send(
            "test@example.com",
            EmailTemplateNames.HEARING_DETAILS,
            buildHearingDateEmail(hearingDetailsRequest, caseData),
            LanguagePreference.english
        );
    }

    private EmailTemplateVars buildHearingDateEmail(HearingDetailsRequest hearingDetailsRequest, CaseData caseData) {
        return HearingDetailsEmail.builder()
            .caseReference(hearingDetailsRequest.getCaseId())
            .caseName(caseData.getApplicantCaseName())
            .partyName(hearingDetailsRequest.getPartyName())
            .hearingDetailsPageLink(hearingDetailsUrl)
            .build();
    }
}
