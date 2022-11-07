package uk.gov.hmcts.reform.prl.services.hearingmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
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
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class HearingManagementService {

    private static final String HMC_STATUS = "LISTED";
    public static final String HEARING_STATE_CHANGE_SUCCESS = "hmcCaseUpdateSuccess";
    public static final String HEARING_STATE_CHANGE_FAILURE = "hmcCaseUpdateFailure";

    @Autowired
    private AuthorisationService authorisationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SystemUserService systemUserService;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;
    @Autowired
    private EmailService emailService;

    @Value("${citizen.url}")
    private String hearingDetailsUrl;

    public void stateChangeForHearingManagement(HearingRequest hearingRequest, String s2sToken) {

        log.info("Processing the callback for the caseId {} with HMC status {}", hearingRequest.getCaseRef(),
                     hearingRequest.getHearingUpdate().getHmcStatus());

        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);
        log.info("Fetching the Case details based on caseId {}", hearingRequest.getCaseRef()
        );

        CaseDetails caseDetails = coreCaseDataApi.getCase(
            userToken,
            s2sToken,
            hearingRequest.getCaseRef()
        );
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        createEvent(hearingRequest, userToken, s2sToken, systemUpdateUserId,
                    HMC_STATUS.equals(hearingRequest.getHearingUpdate().getHmcStatus())
                        ? HEARING_STATE_CHANGE_SUCCESS : HEARING_STATE_CHANGE_FAILURE, caseData
        );

    }

    private void createEvent(HearingRequest hearingRequest, String userToken, String s2sToken,
                             String systemUpdateUserId, String eventId, CaseData caseData) {

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            userToken,
            s2sToken,
            systemUpdateUserId,
            JURISDICTION,
            CASE_TYPE,
            hearingRequest.getCaseRef(),
            eventId
        );

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(caseData)
            .build();

        coreCaseDataApi.submitEventForCaseWorker(
            userToken,
            s2sToken,
            systemUpdateUserId,
            JURISDICTION,
            CASE_TYPE,
            hearingRequest.getCaseRef(),
            true,
            caseDataContent
        );
    }

    public void sendHearingDetailsEmailToCitizen(HearingDetailsRequest hearingDetailsRequest, CaseData caseData) {

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
