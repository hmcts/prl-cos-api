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
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
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

    @Value("${xui.url}")
    private String manageCaseUrl;

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

        if (isStateChanged) {
            sendHearingDetailsEmail(caseData, hearingRequest);
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

    public void updateTabsAfterStateChange(Map<String, Object> data, Long id) throws Exception {
        data.put("id", String.valueOf(id));
        CaseData caseData = objectMapper.convertValue(data, CaseData.class);
        allTabService.updateAllTabsIncludingConfTab(caseData);
    }

    public void sendHearingDetailsEmail(CaseData caseData, HearingRequest hearingRequest) {

        List<String> emailList = new ArrayList<>();

        if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            List<PartyDetails> applicants = caseData
                .getApplicants()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            List<String> applicantsEmailList = applicants.stream()
                .map(PartyDetails::getEmail)
                .collect(Collectors.toList());

            applicantsEmailList.forEach(email -> log.info("Applicant Email:: {}", email));
            applicantsEmailList.forEach(email -> emailService.send(
                email,
                EmailTemplateNames.HEARING_DETAILS,
                buildHearingDetailsEmail(caseData),
                LanguagePreference.english
            ));

            List<PartyDetails> respondents = caseData
                .getRespondents()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            List<String> respondentsEmailList = respondents.stream()
                .filter(respondent -> null != respondent.getEmail()
                    && YesOrNo.Yes.equals(respondent.getCanYouProvideEmailAddress()))
                .map(PartyDetails::getEmail)
                .collect(Collectors.toList());

            applicantsEmailList.forEach(email -> log.info("respondent Email:: {}", email));
            log.info("Building email data:: {}", buildHearingDetailsEmail(caseData));
            if (!respondentsEmailList.isEmpty()) {
                respondentsEmailList.forEach(email -> emailService.send(
                    email,
                    EmailTemplateNames.HEARING_DETAILS,
                    buildHearingDetailsEmail(caseData),
                    LanguagePreference.english
                ));
            }

            List<String> applicantSolicitorsEmailList = applicants.stream()
                .map(PartyDetails::getSolicitorEmail)
                .collect(Collectors.toList());

            applicantSolicitorsEmailList.forEach(email -> emailService.send(
                email,
                EmailTemplateNames.APPLICANT_SOLICITOR_HEARING_DETAILS,
                buildApplicantSolicitorHearingDetailsEmail(caseData, hearingRequest),
                LanguagePreference.english
            ));

            List<String> respondentSolicitorsEmailList = respondents.stream()
                .filter(respondentSolicitor -> null != respondentSolicitor.getSolicitorEmail()
                    && YesOrNo.Yes.equals(respondentSolicitor.getDoTheyHaveLegalRepresentation()))
                .map(PartyDetails::getSolicitorEmail)
                .collect(Collectors.toList());

            if (!respondentSolicitorsEmailList.isEmpty()) {
                respondentSolicitorsEmailList.forEach(email -> emailService.send(
                    email,
                    EmailTemplateNames.RESPONDENT_SOLICITOR_HEARING_DETAILS,
                    buildRespondentSolicitorHearingDetailsEmail(caseData, hearingRequest),
                    LanguagePreference.english
                ));
            }
        } else {

            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();
            PartyDetails fl401Respondent = caseData
                .getRespondentsFL401();
            emailList.add(fl401Applicant.getEmail());
            emailList.add(fl401Respondent.getEmail());

            emailService.send(
                fl401Applicant.getSolicitorEmail(),
                EmailTemplateNames.APPLICANT_SOLICITOR_HEARING_DETAILS,
                buildApplicantSolicitorHearingDetailsEmail(caseData, hearingRequest),
                LanguagePreference.english
            );
        }
    }

    private EmailTemplateVars buildApplicantSolicitorHearingDetailsEmail(CaseData caseData, HearingRequest hearingRequest) {

        HearingDetailsEmail hearingDetailsEmail = null;

        if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            List<PartyDetails> applicants = caseData
                .getApplicants()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            List<String> applicantSolicitorNamesList = applicants.stream()
                .map(element -> element.getRepresentativeFirstName() + " " + element.getRepresentativeLastName())
                .collect(Collectors.toList());

            for (String applicantSolicitorName : applicantSolicitorNamesList) {

                hearingDetailsEmail = HearingDetailsEmail.builder()
                    .caseReference(String.valueOf(caseData.getId()))
                    .caseName(caseData.getApplicantCaseName())
                    .issueDate(String.valueOf(caseData.getIssueDate()))
                    .typeOfHearing(" ")
                    .hearingDateAndTime(String.valueOf(hearingRequest.getHearingUpdate().getNextHearingDate()))
                    .hearingVenue(hearingRequest.getHearingUpdate().getHearingVenueId())
                    .partySolicitorName(applicantSolicitorName)
                    .build();
            }
        } else {
            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();
            String applicantSolicitorName = fl401Applicant.getRepresentativeFirstName() + " "
                + fl401Applicant.getRepresentativeLastName();

            hearingDetailsEmail = HearingDetailsEmail.builder()
                .caseReference(String.valueOf(caseData.getId()))
                .caseName(caseData.getApplicantCaseName())
                .issueDate(String.valueOf(caseData.getIssueDate()))
                .typeOfHearing(" ")
                .hearingDateAndTime(String.valueOf(hearingRequest.getHearingUpdate().getNextHearingDate()))
                .hearingVenue(hearingRequest.getHearingUpdate().getHearingVenueId())
                .partySolicitorName(applicantSolicitorName)
                .build();
        }

        return hearingDetailsEmail;
    }

    private EmailTemplateVars buildRespondentSolicitorHearingDetailsEmail(CaseData caseData, HearingRequest hearingRequest) {
        HearingDetailsEmail hearingDetailsEmail = null;

        List<PartyDetails> respondents = caseData
            .getRespondents()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> respondentSolicitorNamesList = respondents.stream()
            .map(element -> element.getRepresentativeFirstName() + " " + element.getRepresentativeLastName())
            .collect(Collectors.toList());

        for (String respondentSolicitorName: respondentSolicitorNamesList) {

            hearingDetailsEmail = HearingDetailsEmail.builder()
                .caseReference(String.valueOf(caseData.getId()))
                .caseName(caseData.getApplicantCaseName())
                .issueDate(String.valueOf(caseData.getIssueDate()))
                .typeOfHearing(" ")
                .hearingDateAndTime(String.valueOf(hearingRequest.getHearingUpdate().getNextHearingDate()))
                .hearingVenue(hearingRequest.getHearingUpdate().getHearingVenueId())
                .partySolicitorName(respondentSolicitorName)
                .build();
        }

        return hearingDetailsEmail;
    }

    private EmailTemplateVars buildHearingDetailsEmail(CaseData caseData) {

        HearingDetailsEmail hearingDetailsEmail = null;
        List<String> partyNamesList = new ArrayList<>();

        if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            List<PartyDetails> applicants = caseData
                .getApplicants()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            List<String> applicantNamesList = applicants.stream()
                .map(element -> element.getFirstName() + " " + element.getLastName())
                .collect(Collectors.toList());

            List<PartyDetails> respondents = caseData
                .getRespondents()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            List<String> respondentNamesList = respondents.stream()
                .map(element -> element.getFirstName() + " " + element.getLastName())
                .collect(Collectors.toList());

            partyNamesList.add(String.valueOf(applicantNamesList));
            partyNamesList.add(String.valueOf(respondentNamesList));
        } else {

            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();
            PartyDetails fl401Respondent = caseData
                .getRespondentsFL401();

            partyNamesList.add(fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName());
            partyNamesList.add(fl401Respondent.getFirstName() + " " + fl401Respondent.getLastName());
        }

        for (String partyName: partyNamesList) {
            hearingDetailsEmail = HearingDetailsEmail.builder()
                .caseReference(String.valueOf(caseData.getId()))
                .caseName(caseData.getApplicantCaseName())
                .partyName(partyName)
                .hearingDetailsPageLink(manageCaseUrl + "/" + caseData.getId())
                .build();
        }

        return hearingDetailsEmail;
    }

}
