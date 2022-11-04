package uk.gov.hmcts.reform.prl.controllers.hearingmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.exception.HearingManagementValidationException;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.HearingDetailsRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingRequest;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.HearingDetailsEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.hearingmanagement.HearingManagementService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class HearingsManagementController {

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

    @Autowired
    private AuthorisationService authorisationService;

    @Autowired
    private SystemUserService systemUserService;

    @Autowired
    private HearingManagementService hearingManagementService;

    @Value("${citizen.url}")
    private String hearingDetailsUrl;

    @PutMapping(path = "/hearing-management-state-update", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Ways to pay will call this API and send the status of payment with other details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public void hearingManagementStateUpdate(@RequestHeader("serviceAuthorization") String s2sToken,
                                     @RequestBody HearingRequest hearingRequest) throws Exception {

        if (Boolean.FALSE.equals(authorisationService.authoriseService(s2sToken))) {
            throw new HearingManagementValidationException("Provide a valid s2s token");
        } else {
            hearingManagementService.stateChangeForHearingManagement(hearingRequest, s2sToken);
        }

    }

    @PostMapping(path = "/hearing-details-notification", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
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
            .state(State.DECISION_OUTCOME)
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

        String email = "";
        String  partyId = hearingDetailsRequest.getPartyId();
        if (caseData.getCaseTypeOfApplication().equals(C100_CASE_TYPE)) {
            Optional<PartyDetails> applicantPartyDetails = caseData.getApplicants()

                .stream()
                .filter(applicant -> applicant.getId().equals(partyId))
                .map(Element::getValue)
                .findFirst();
            if (applicantPartyDetails.isPresent()) {
                if (applicantPartyDetails.get().getCanYouProvideEmailAddress().equals(YesOrNo.Yes)) {
                    email = applicantPartyDetails.get().getEmail();
                }
            }

            Optional<PartyDetails> respondenytPartyDetails = caseData.getRespondents()
                .stream()
                .filter(respondent -> respondent.getId().equals(partyId))
                .map(Element::getValue)
                .findFirst();

            if (respondenytPartyDetails.isPresent() && email.isEmpty()) {
                if (respondenytPartyDetails.get().getCanYouProvideEmailAddress().equals(YesOrNo.Yes)) {
                    email = respondenytPartyDetails.get().getEmail();
                }
            }
        } else {
            if (caseData.getApplicantsFL401().getCanYouProvideEmailAddress().equals(YesOrNo.Yes)) {
                email = caseData.getApplicantsFL401().getEmail();
            }
            if (caseData.getRespondentsFL401().getCanYouProvideEmailAddress().equals(YesOrNo.Yes) && email.isEmpty()) {
                email = caseData.getRespondentsFL401().getEmail();
            }
        }

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
