package uk.gov.hmcts.reform.prl.services.hearingmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingRequest;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.HearingDetailsEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ADJOURNED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWAITING_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CANCELLED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COMPLETED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POSTPONED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WAITING_TO_BE_LISTED;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingManagementService {

    public static final String HEARING_STATE_CHANGE_SUCCESS = "hmcCaseUpdateSuccess";
    public static final String HEARING_STATE_CHANGE_FAILURE = "hmcCaseUpdateFailure";

    private final AuthorisationService authorisationService;
    private final ObjectMapper objectMapper;
    private final SystemUserService systemUserService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final EmailService emailService;
    private final AllTabServiceImpl allTabService;

    @Value("${citizen.url}")
    private String hearingDetailsUrl;

    public void stateChangeForHearingManagement(HearingRequest hearingRequest) throws Exception {

        log.info("Processing the callback for the caseId {} with HMC status {}", hearingRequest.getCaseRef(),
                     hearingRequest.getHearingUpdate().getHmcStatus());

        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);
        log.info("Fetching the Case details based on caseId {}", hearingRequest.getCaseRef()
        );

        CaseDetails caseDetails = coreCaseDataApi.getCase(
            userToken,
            authTokenGenerator.generate(),
            hearingRequest.getCaseRef()
        );

        log.info("Retreiving thecase details: {}",caseDetails);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        String hmcStatus = hearingRequest.getHearingUpdate().getHmcStatus();
        boolean isStateChanged = false;

        switch (hmcStatus) {
            case AWAITING_HEARING_DETAILS:
                CaseDetails caseDetailsUpdate = createEvent(hearingRequest, userToken, systemUpdateUserId,
                            HEARING_STATE_CHANGE_SUCCESS, caseData);
                isStateChanged = true;
                updateTabsAfterStateChange(caseDetailsUpdate.getData(), caseDetailsUpdate.getId());
                break;
            case LISTED:
            case WAITING_TO_BE_LISTED:
            case CANCELLED:
            case ADJOURNED:
            case POSTPONED:
            case COMPLETED:
                CaseDetails updatedCaseDetails = createEvent(hearingRequest, userToken, systemUpdateUserId,
                            HEARING_STATE_CHANGE_FAILURE, caseData);
                isStateChanged = true;
                updateTabsAfterStateChange(updatedCaseDetails.getData(), updatedCaseDetails.getId());
                break;
            default:
                break;
        }

        allTabService.updateAllTabsIncludingConfTab(caseData);

        if (isStateChanged) {
            sendHearingDetailsEmailToCitizen(caseData);

        }

    }

    private CaseDetails createEvent(HearingRequest hearingRequest, String userToken,
                             String systemUpdateUserId, String eventId, CaseData caseData) {

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
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

        return  coreCaseDataApi.submitEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            JURISDICTION,
            CASE_TYPE,
            hearingRequest.getCaseRef(),
            true,
            caseDataContent
        );
    }

    public void sendHearingDetailsEmailToCitizen(CaseData caseData) {

        List<String> emailList = new ArrayList<>();

        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> applicantEmailList = applicants.stream()
            .map(PartyDetails::getEmail)
            .collect(Collectors.toList());

        List<PartyDetails> respondents = caseData
            .getRespondents()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> respondentEmailList = respondents.stream()
            .map(PartyDetails::getEmail)
            .collect(Collectors.toList());

        emailList.add(String.valueOf(applicantEmailList));
        emailList.add(String.valueOf(respondentEmailList));

        PartyDetails fl401Applicant = caseData
            .getApplicantsFL401();
        PartyDetails fl401Respondent = caseData
            .getRespondentsFL401();
        emailList.add(fl401Applicant.getEmail());
        emailList.add(fl401Respondent.getEmail());

        emailList.forEach(email ->   emailService.send(
            email,
            EmailTemplateNames.HEARING_DETAILS,
            buildHearingDateEmail(caseData),
            LanguagePreference.english
        ));
    }

    private EmailTemplateVars buildHearingDateEmail(CaseData caseData) {
        return HearingDetailsEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .hearingDetailsPageLink(hearingDetailsUrl)
            .build();
    }

    public void updateTabsAfterStateChange(Map<String, Object> data, Long id) throws Exception {
        data.put("id", String.valueOf(id));
        CaseData caseData = objectMapper.convertValue(data, CaseData.class);
        allTabService.updateAllTabsIncludingConfTab(caseData);
    }
}
