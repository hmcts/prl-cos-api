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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Value("${citizen.url}")
    private String dashboardUrl;
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

        switch (hmcStatus) {
            case LISTED:
                CaseDetails listedCaseDetails = createEvent(hearingRequest, userToken, systemUpdateUserId,
                            HEARING_STATE_CHANGE_SUCCESS, caseData);
                updateTabsAfterStateChange(listedCaseDetails.getData(), listedCaseDetails.getId());
                sendHearingDetailsEmail(caseData, hearingRequest);
                break;

            case CANCELLED:
                CaseDetails cancelledCaseDetails = createEvent(hearingRequest, userToken, systemUpdateUserId,
                                                             HEARING_STATE_CHANGE_FAILURE, caseData);
                updateTabsAfterStateChange(cancelledCaseDetails.getData(), cancelledCaseDetails.getId());
                sendHearingCancelledEmail(caseData, hearingRequest);
                break;
            case WAITING_TO_BE_LISTED:
            case COMPLETED:
            case POSTPONED:
            case ADJOURNED:
                CaseDetails completedCaseDetails = createEvent(hearingRequest, userToken, systemUpdateUserId,
                            HEARING_STATE_CHANGE_FAILURE, caseData);
                updateTabsAfterStateChange(completedCaseDetails.getData(), completedCaseDetails.getId());
                sendHearingChangeDetailsEmail(caseData, hearingRequest);
                break;
            default:
                break;
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

    private void updateTabsAfterStateChange(Map<String, Object> data, Long id) {
        data.put("id", String.valueOf(id));
        CaseData caseData = objectMapper.convertValue(data, CaseData.class);
        allTabService.updateAllTabsIncludingConfTab(caseData);
    }

    private void sendHearingChangeDetailsEmail(CaseData caseData, HearingRequest hearingRequest) {

        if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            List<PartyDetails> applicants = caseData
                .getApplicants()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            List<String> applicantsEmailList = applicants.stream()
                .map(PartyDetails::getEmail)
                .collect(Collectors.toList());

            List<String> partyNamesList = applicants.stream()
                .map(element -> element.getFirstName() + " " + element.getLastName())
                .collect(Collectors.toList());

            applicantsEmailList.forEach(email -> log.info("Applicant Email:: {}", email));
            for (String email: applicantsEmailList) {

                Optional<String> partyName = applicants.stream()
                    .filter(applicantEmail -> applicantEmail.equals(email))
                    .map(element -> element.getFirstName() + " " + element.getLastName())
                    .findFirst();
                emailService.send(
                    email,
                    EmailTemplateNames.HEARING_DETAILS,
                    buildApplicantHearingDetailsEmail(caseData, String.valueOf(partyName)),
                    LanguagePreference.english
                );
            }

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

        } else {
            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();
            String applicantName = fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName();
            PartyDetails fl401Respondent = caseData
                .getRespondentsFL401();
            String respondentName = fl401Respondent.getFirstName() + " " + fl401Respondent.getLastName();

            emailService.send(
                fl401Applicant.getEmail(),
                EmailTemplateNames.HEARING_DETAILS,
                buildApplicantHearingDetailsEmail(caseData, applicantName),
                LanguagePreference.english
            );

            emailService.send(
                fl401Respondent.getEmail(),
                EmailTemplateNames.HEARING_DETAILS,
                buildRespondentHearingDetailsEmail(caseData),
                LanguagePreference.english
            );

            emailService.send(
                fl401Applicant.getSolicitorEmail(),
                EmailTemplateNames.APPLICANT_SOLICITOR_HEARING_DETAILS,
                buildApplicantSolicitorHearingDetailsEmail(caseData, hearingRequest),
                LanguagePreference.english
            );
        }
    }

    private void sendHearingCancelledEmail(CaseData caseData, HearingRequest hearingRequest) {

        if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            List<PartyDetails> applicants = caseData
                .getApplicants()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            List<String> applicantsEmailList = applicants.stream()
                .map(PartyDetails::getEmail)
                .collect(Collectors.toList());

            List<String> partyNamesList = applicants.stream()
                .map(element -> element.getFirstName() + " " + element.getLastName())
                .collect(Collectors.toList());

            applicantsEmailList.forEach(email -> log.info("Applicant Email:: {}", email));
            for (String email: applicantsEmailList) {
                partyNamesList.forEach(partyName -> emailService.send(
                    email,
                    EmailTemplateNames.HEARING_DETAILS,
                    buildApplicantHearingDetailsEmail(caseData, partyName),
                    LanguagePreference.english
                ));
            }

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

        } else {
            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();
            String applicantName = fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName();
            PartyDetails fl401Respondent = caseData
                .getRespondentsFL401();
            String respondentName = fl401Respondent.getFirstName() + " " + fl401Respondent.getLastName();

            emailService.send(
                fl401Applicant.getEmail(),
                EmailTemplateNames.HEARING_DETAILS,
                buildApplicantHearingDetailsEmail(caseData, applicantName),
                LanguagePreference.english
            );

            emailService.send(
                fl401Respondent.getEmail(),
                EmailTemplateNames.HEARING_DETAILS,
                buildRespondentHearingDetailsEmail(caseData),
                LanguagePreference.english
            );

            emailService.send(
                fl401Applicant.getSolicitorEmail(),
                EmailTemplateNames.APPLICANT_SOLICITOR_HEARING_DETAILS,
                buildApplicantSolicitorHearingDetailsEmail(caseData, hearingRequest),
                LanguagePreference.english
            );
        }
    }

    private void sendHearingDetailsEmail(CaseData caseData, HearingRequest hearingRequest) {

        if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            List<PartyDetails> applicants = caseData
                .getApplicants()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            List<String> applicantsEmailList = applicants.stream()
                .map(PartyDetails::getEmail)
                .collect(Collectors.toList());

            List<String> partyNamesList = applicants.stream()
                .map(element -> element.getFirstName() + " " + element.getLastName())
                .collect(Collectors.toList());

            applicantsEmailList.forEach(email -> log.info("Applicant Email:: {}", email));
            for (String email: applicantsEmailList) {

                Optional<String> partyName = applicants.stream()
                    .filter(applicantEmail -> applicantEmail.equals(email))
                    .map(element -> element.getFirstName() + " " + element.getLastName())
                    .findFirst();
                emailService.send(
                    email,
                    EmailTemplateNames.HEARING_DETAILS,
                    buildApplicantHearingDetailsEmail(caseData, String.valueOf(partyName)),
                    LanguagePreference.english
                );
            }

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
            String applicantName = fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName();
            PartyDetails fl401Respondent = caseData
                .getRespondentsFL401();
            String respondentName = fl401Respondent.getFirstName() + " " + fl401Respondent.getLastName();

            emailService.send(
                fl401Applicant.getEmail(),
                EmailTemplateNames.HEARING_DETAILS,
                buildApplicantHearingDetailsEmail(caseData, applicantName),
                LanguagePreference.english
            );

            emailService.send(
                fl401Respondent.getEmail(),
                EmailTemplateNames.HEARING_DETAILS,
                buildRespondentHearingDetailsEmail(caseData),
                LanguagePreference.english
            );


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


            for (String applicantSolicitorName : applicantSolicitorNamesList) {

                hearingDetailsEmail = HearingDetailsEmail.builder()
                    .caseReference(String.valueOf(caseData.getId()))
                    .caseName(caseData.getApplicantCaseName())
                    .issueDate(String.valueOf(issueDate))
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

    private EmailTemplateVars buildApplicantHearingDetailsEmail(CaseData caseData, String partyName) {

        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        return HearingDetailsEmail.builder()
                .caseReference(String.valueOf(caseData.getId()))
                .caseName(caseData.getApplicantCaseName())
                .partyName(partyName)
                .hearingDetailsPageLink(manageCaseUrl + "/" + caseData.getId())
                .build();
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
                .hearingDetailsPageLink(dashboardUrl)
                .build();
        }
        return hearingDetailsEmail;
    }
}
