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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ADJOURNED;
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
    private static final String DATE_FORMAT = "dd-MM-yyyy";

    private final AuthorisationService authorisationService;
    private final ObjectMapper objectMapper;
    private final SystemUserService systemUserService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final EmailService emailService;
    private final AllTabServiceImpl allTabService;

    @Value("${xui.url}")
    private String manageCaseUrl;
    private LocalDate issueDate;

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

        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        issueDate = caseData.getIssueDate();
        log.info("Retrieving issuedate from casedata: {}",issueDate);

        String hmcStatus = hearingRequest.getHearingUpdate().getHmcStatus();
        boolean isStateChanged = false;

        switch (hmcStatus) {
            case LISTED:
                CaseDetails listedCaseDetails = createEvent(hearingRequest, userToken, systemUpdateUserId,
                            HEARING_STATE_CHANGE_SUCCESS, caseData);
                isStateChanged = true;
                updateTabsAfterStateChange(listedCaseDetails.getData(), listedCaseDetails.getId());
                break;

            case CANCELLED:
                CaseDetails cancelledCaseDetails = createEvent(hearingRequest, userToken, systemUpdateUserId,
                                                             HEARING_STATE_CHANGE_FAILURE, caseData);
                isStateChanged = true;
                updateTabsAfterStateChange(cancelledCaseDetails.getData(), cancelledCaseDetails.getId());
                break;
            case WAITING_TO_BE_LISTED:
            case ADJOURNED:
            case POSTPONED:
            case COMPLETED:
                CaseDetails completedCaseDetails = createEvent(hearingRequest, userToken, systemUpdateUserId,
                            HEARING_STATE_CHANGE_FAILURE, caseData);
                isStateChanged = true;
                updateTabsAfterStateChange(completedCaseDetails.getData(), completedCaseDetails.getId());
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
                buildApplicantHearingDetailsEmail(caseData),
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

            respondentsEmailList.forEach(email -> log.info("respondent Email:: {}", email));
            log.info("Building email data:: {}", buildRespondentHearingDetailsEmail(caseData));
            if (!respondentsEmailList.isEmpty()) {
                respondentsEmailList.forEach(email -> emailService.send(
                    email,
                    EmailTemplateNames.HEARING_DETAILS,
                    buildRespondentHearingDetailsEmail(caseData),
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
                .map(PartyDetails::getSolicitorEmail)
                .collect(Collectors.toList());

            if (!respondentSolicitorsEmailList.isEmpty() && !respondentSolicitorsEmailList.contains(null)) {
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

            log.info("Issue date:{}====", caseData.getIssueDate());

            LocalDate issueDate = LocalDate.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

            for (String applicantSolicitorName : applicantSolicitorNamesList) {

                hearingDetailsEmail = HearingDetailsEmail.builder()
                    .caseReference(String.valueOf(caseData.getId()))
                    .caseName(caseData.getApplicantCaseName())
                    .issueDate(issueDate.format(dateTimeFormatter))
                    .typeOfHearing(" ")
                    .hearingDateAndTime(String.valueOf(hearingRequest.getHearingUpdate().getNextHearingDate()))
                    .hearingVenue(hearingRequest.getHearingUpdate().getHearingVenueName())
                    .partySolicitorName(applicantSolicitorName)
                    .build();
            }
        } else {
            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();
            String applicantSolicitorName = fl401Applicant.getRepresentativeFirstName() + " "
                + fl401Applicant.getRepresentativeLastName();

            LocalDate issueDate = LocalDate.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

            hearingDetailsEmail = HearingDetailsEmail.builder()
                .caseReference(String.valueOf(caseData.getId()))
                .caseName(caseData.getApplicantCaseName())
                .issueDate(issueDate.format(dateTimeFormatter))
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

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        log.info("Issue date from casedata: {}", caseData.getIssueDate());
        for (String respondentSolicitorName: respondentSolicitorNamesList) {

            hearingDetailsEmail = HearingDetailsEmail.builder()
                .caseReference(String.valueOf(caseData.getId()))
                .caseName(caseData.getApplicantCaseName())
                .issueDate(issueDate.format(dateTimeFormatter))
                .typeOfHearing(" ")
                .hearingDateAndTime(String.valueOf(hearingRequest.getHearingUpdate().getNextHearingDate()))
                .hearingVenue(hearingRequest.getHearingUpdate().getHearingVenueId())
                .partySolicitorName(respondentSolicitorName)
                .build();
        }

        return hearingDetailsEmail;
    }

    private EmailTemplateVars buildApplicantHearingDetailsEmail(CaseData caseData) {

        HearingDetailsEmail hearingDetailsEmail = null;
        if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            List<PartyDetails> applicants = caseData
                .getApplicants()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            List<String> applicantNamesList = applicants.stream()
                .map(element -> element.getFirstName() + " " + element.getLastName())
                .collect(Collectors.toList());

            for (String partyName: applicantNamesList) {
                hearingDetailsEmail = HearingDetailsEmail.builder()
                    .caseReference(String.valueOf(caseData.getId()))
                    .caseName(caseData.getApplicantCaseName())
                    .partyName(partyName)
                    .hearingDetailsPageLink(manageCaseUrl + "/" + caseData.getId())
                    .build();
            }

        } else {
            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();

            hearingDetailsEmail = HearingDetailsEmail.builder()
                .caseReference(String.valueOf(caseData.getId()))
                .caseName(caseData.getApplicantCaseName())
                .partyName(fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName())
                .hearingDetailsPageLink(manageCaseUrl + "/" + caseData.getId())
                .build();
        }
        return hearingDetailsEmail;
    }

    private EmailTemplateVars buildRespondentHearingDetailsEmail(CaseData caseData) {

        HearingDetailsEmail hearingDetailsEmail = null;
        if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            List<PartyDetails> respondents = caseData
                .getRespondents()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            List<String> respondentNamesList = respondents.stream()
                .map(element -> element.getFirstName() + " " + element.getLastName())
                .collect(Collectors.toList());

            log.info("respondents names List {}", respondentNamesList);

            for (String partyName: respondentNamesList) {
                log.info("PartyName:******{}******", partyName);

                hearingDetailsEmail = HearingDetailsEmail.builder()
                    .caseReference(String.valueOf(caseData.getId()))
                    .caseName(caseData.getApplicantCaseName())
                    .partyName(partyName)
                    .hearingDetailsPageLink(manageCaseUrl + "/" + caseData.getId())
                    .build();
            }

        } else {

            PartyDetails fl401Respondent = caseData
                .getRespondentsFL401();
            hearingDetailsEmail = HearingDetailsEmail.builder()
                .caseReference(String.valueOf(caseData.getId()))
                .caseName(caseData.getApplicantCaseName())
                .partyName(fl401Respondent.getFirstName() + " " + fl401Respondent.getLastName())
                .hearingDetailsPageLink(manageCaseUrl + "/" + caseData.getId())
                .build();
        }
        return hearingDetailsEmail;
    }
}
