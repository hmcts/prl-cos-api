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
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.State;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COMPLETED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POSTPONED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WAITING_TO_BE_LISTED;
import static uk.gov.hmcts.reform.prl.enums.State.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.prl.enums.State.PREPARE_FOR_HEARING_CONDUCT_HEARING;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingManagementService {
    private static final String DATE_FORMAT = "dd-MM-yyyy";

    private final AuthorisationService authorisationService;
    private final ObjectMapper objectMapper;
    private final SystemUserService systemUserService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final EmailService emailService;
    private final AllTabServiceImpl allTabService;
    private final CoreCaseDataService coreCaseDataService;

    private  StartEventResponse startEventResponse;
    private  EventRequestData eventRequestData;
    private  CaseDetails caseDetails;

    private CaseDataContent caseDataContent;

    @Value("${xui.url}")
    private String manageCaseUrl;

    @Value("${citizen.url}")
    private String dashboardUrl;

    public void caseStateChangeForHearingManagement(HearingRequest hearingRequest) throws Exception {

        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);

        log.info("Processing the callback for the caseId {} with HMC status {}", hearingRequest.getCaseRef(),
                     hearingRequest.getHearingUpdate().getHmcStatus());

        log.info("Fetching the Case details based on caseId {}", hearingRequest.getCaseRef()
        );

        String hmcStatus = hearingRequest.getHearingUpdate().getHmcStatus();
        switch (hmcStatus) {
            case LISTED:
                CaseEvent caseEvent = CaseEvent.HEARING_STATE_CHANGE_SUCCESS;
                State state = DECISION_OUTCOME;
                eventRequestData = coreCaseDataService.eventRequest(caseEvent, systemUpdateUserId);

                startEventResponse =
                    coreCaseDataService.startUpdate(
                        userToken,
                        eventRequestData,
                        hearingRequest.getCaseRef(),
                        true
                    );
                //updating State in CaseData
                startEventResponse.getCaseDetails().getData().put(STATE_FIELD,state.getValue());

                CaseData caseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);
                caseDataContent = coreCaseDataService.createCaseDataContent(
                    startEventResponse,
                    caseData
                );

                caseDetails = coreCaseDataService.submitUpdate(
                    userToken,
                    eventRequestData,
                    caseDataContent,
                    hearingRequest.getCaseRef(),
                    true
                );
                updateTabsAfterStateChange(caseDetails.getData(), caseDetails.getId());

                //retrieving  latest case data received from submitUpdate response
                CaseData caseDataUpdated = CaseUtils.getCaseData(caseDetails, objectMapper);

                sendHearingDetailsEmail(caseDataUpdated, hearingRequest);
                break;

            case CANCELLED:
                caseEvent = CaseEvent.HEARING_STATE_CHANGE_FAILURE;
                state = PREPARE_FOR_HEARING_CONDUCT_HEARING;
                eventRequestData = coreCaseDataService.eventRequest(caseEvent, systemUpdateUserId);

                startEventResponse =
                    coreCaseDataService.startUpdate(
                        userToken,
                        eventRequestData,
                        hearingRequest.getCaseRef(),
                        true
                    );

                //updating State in CaseData
                startEventResponse.getCaseDetails().getData().put(STATE_FIELD,state.getValue());

                caseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);
                caseDataContent = coreCaseDataService.createCaseDataContent(
                    startEventResponse,
                    caseData
                );

                CaseDetails cancelledCaseDetails = coreCaseDataService.submitUpdate(
                    userToken,
                    eventRequestData,
                    caseDataContent,
                    hearingRequest.getCaseRef(),
                    true
                );

                updateTabsAfterStateChange(cancelledCaseDetails.getData(), cancelledCaseDetails.getId());

                //retrieving  latest case data received from submitUpdate response
                caseDataUpdated = CaseUtils.getCaseData(cancelledCaseDetails, objectMapper);

                sendHearingCancelledEmail(caseDataUpdated);
                break;
            case WAITING_TO_BE_LISTED:
            case COMPLETED:
            case POSTPONED:
            case ADJOURNED:
                caseEvent = CaseEvent.HEARING_STATE_CHANGE_FAILURE;
                state = PREPARE_FOR_HEARING_CONDUCT_HEARING;
                eventRequestData = coreCaseDataService.eventRequest(caseEvent, systemUpdateUserId);

                startEventResponse =
                    coreCaseDataService.startUpdate(
                        userToken,
                        eventRequestData,
                        hearingRequest.getCaseRef(),
                        true
                    );

                //updating State in CaseData
                startEventResponse.getCaseDetails().getData().put(STATE_FIELD,state.getValue());

                caseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);
                caseDataContent = coreCaseDataService.createCaseDataContent(
                    startEventResponse,
                    caseData
                );

                CaseDetails completedCaseDetails = coreCaseDataService.submitUpdate(
                    userToken,
                    eventRequestData,
                    caseDataContent,
                    hearingRequest.getCaseRef(),
                    true
                );

                updateTabsAfterStateChange(completedCaseDetails.getData(), completedCaseDetails.getId());

                //retrieving  latest case data received from submitUpdate response
                caseDataUpdated = CaseUtils.getCaseData(completedCaseDetails, objectMapper);

                sendHearingChangeDetailsEmail(caseDataUpdated);
                break;
            default:
                break;
        }
    }

    private void updateTabsAfterStateChange(Map<String, Object> data, Long id) {
        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);

        data.put("id", String.valueOf(id));

        EventRequestData allTabsUpdateEventRequestData = coreCaseDataService.eventRequest(
            CaseEvent.UPDATE_ALL_TABS,
            systemUpdateUserId
        );
        StartEventResponse allTabsUpdateStartEventResponse =
            coreCaseDataService.startUpdate(
                userToken,
                allTabsUpdateEventRequestData,
                String.valueOf(id),
                true
            );

        CaseData allTabsUpdateCaseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(
            allTabsUpdateStartEventResponse,
            objectMapper
        );

        allTabService.updateAllTabsIncludingConfTabRefactored(
            userToken,
            String.valueOf(id),
            allTabsUpdateStartEventResponse,
            allTabsUpdateEventRequestData,
            allTabsUpdateCaseData
        );

        log.info(
            "Refreshed the tab for caseid {} ",
            String.valueOf(id)
        );

    }

    private void sendHearingChangeDetailsEmail(CaseData caseData) {
        if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            sendHearingChangeDetailsEmailForCA(caseData);

        } else {
            sendHearingChangeDetailsEmailForDA(caseData);
        }
    }

    private void sendHearingChangeDetailsEmailForDA(CaseData caseData) {
        PartyDetails fl401Applicant = caseData
            .getApplicantsFL401();
        String applicantName = fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName();
        PartyDetails fl401Respondent = caseData
            .getRespondentsFL401();
        String respondentName = fl401Respondent.getFirstName() + " " + fl401Respondent.getLastName();

        if (null != fl401Applicant.getEmail()) {
            emailService.send(
                fl401Applicant.getEmail(),
                EmailTemplateNames.HEARING_CHANGES,
                buildApplicantOrRespondentEmail(caseData, applicantName),
                LanguagePreference.english
            );
        }

        if (null != fl401Respondent.getEmail()) {
            emailService.send(
                fl401Respondent.getEmail(),
                EmailTemplateNames.HEARING_CHANGES,
                buildApplicantOrRespondentEmail(caseData, respondentName),
                LanguagePreference.english
            );
        }
    }

    private void sendHearingChangeDetailsEmailForCA(CaseData caseData) {
        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> applicantsEmailList = applicants.stream()
            .map(PartyDetails::getEmail)
            .collect(Collectors.toList());

        if (!applicantsEmailList.isEmpty() && !applicantsEmailList.contains(null)) {
            for (String email : applicantsEmailList) {
                Optional<String> partyName = applicants.stream()
                    .filter(applicantEmail -> applicantEmail.getEmail().equals(email))
                    .map(element -> element.getFirstName() + " " + element.getLastName())
                    .findFirst();
                emailService.send(
                    email,
                    EmailTemplateNames.HEARING_CHANGES,
                    buildApplicantOrRespondentEmail(caseData, String.valueOf(partyName)),
                    LanguagePreference.english
                );
            }
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

        if (!respondentsEmailList.isEmpty() && !respondentsEmailList.contains(null)) {
            for (String email : respondentsEmailList) {
                Optional<String> partyName = respondents.stream()
                    .filter(p -> p.getEmail().equals(email))
                    .map(element -> element.getFirstName() + " " + element.getLastName())
                    .findAny();
                emailService.send(
                    email,
                    EmailTemplateNames.HEARING_CHANGES,
                    buildApplicantOrRespondentEmail(caseData, partyName.orElse(null)),
                    LanguagePreference.english
                );
            }
        }
    }

    private void sendHearingCancelledEmail(CaseData caseData) {
        if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            sendHearingCancelledEmailForCA(caseData);

        } else {
            sendHearingCancelledEmailForDA(caseData);
        }
    }

    private void sendHearingCancelledEmailForDA(CaseData caseData) {
        PartyDetails fl401Applicant = caseData
            .getApplicantsFL401();
        String applicantName = fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName();
        PartyDetails fl401Respondent = caseData
            .getRespondentsFL401();
        String respondentName = fl401Respondent.getFirstName() + " " + fl401Respondent.getLastName();

        if (null != fl401Applicant.getEmail()) {

            emailService.send(
                fl401Applicant.getEmail(),
                EmailTemplateNames.HEARING_CANCELLED,
                buildApplicantOrRespondentEmail(caseData, applicantName),
                LanguagePreference.english
            );
        }

        if (null != fl401Respondent.getEmail()) {
            emailService.send(
                fl401Respondent.getEmail(),
                EmailTemplateNames.HEARING_CANCELLED,
                buildApplicantOrRespondentEmail(caseData, respondentName),
                LanguagePreference.english
            );
        }
    }

    private void sendHearingCancelledEmailForCA(CaseData caseData) {
        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> applicantsEmailList = applicants.stream()
            .map(PartyDetails::getEmail)
            .collect(Collectors.toList());

        if (!applicantsEmailList.isEmpty() && !applicantsEmailList.contains(null)) {
            for (String email : applicantsEmailList) {
                Optional<String> partyName = applicants.stream()
                    .filter(p -> p.getEmail().equals(email))
                    .map(element -> element.getFirstName() + " " + element.getLastName())
                    .findAny();
                emailService.send(
                    email,
                    EmailTemplateNames.HEARING_CANCELLED,
                    buildApplicantOrRespondentEmail(caseData, partyName.orElse(null)),
                    LanguagePreference.english
                );
            }
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

        if (!respondentsEmailList.isEmpty() && !respondentsEmailList.contains(null)) {
            for (String email : respondentsEmailList) {
                Optional<String> partyName = respondents.stream()
                    .filter(p -> p.getEmail().equals(email))
                    .map(element -> element.getFirstName() + " " + element.getLastName())
                    .findAny();
                emailService.send(
                    email,
                    EmailTemplateNames.HEARING_CANCELLED,
                    buildApplicantOrRespondentEmail(caseData, partyName.orElse(null)),
                    LanguagePreference.english
                );
            }
        }
    }

    private void sendHearingDetailsEmail(CaseData caseData, HearingRequest hearingRequest) {
        if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            sendHearingDetailsEmailForCA(caseData, hearingRequest);
        } else {
            sendHearingDetailsEmailForDA(caseData, hearingRequest);
        }
    }

    private void sendHearingDetailsEmailForDA(CaseData caseData, HearingRequest hearingRequest) {
        PartyDetails fl401Applicant = caseData
            .getApplicantsFL401();
        String applicantName = fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName();
        PartyDetails fl401Respondent = caseData
            .getRespondentsFL401();
        String respondentName = fl401Respondent.getFirstName() + " " + fl401Respondent.getLastName();
        String applicantSolicitorName = fl401Applicant.getRepresentativeFirstName() + " "
            + fl401Applicant.getRepresentativeLastName();

        if (null != fl401Applicant.getEmail()) {
            emailService.send(
                fl401Applicant.getEmail(),
                EmailTemplateNames.HEARING_DETAILS,
                buildApplicantOrRespondentEmail(caseData, applicantName),
                LanguagePreference.english
            );
        }

        if (null != fl401Respondent.getEmail()) {
            emailService.send(
                fl401Respondent.getEmail(),
                EmailTemplateNames.HEARING_DETAILS,
                buildApplicantOrRespondentEmail(caseData, respondentName),
                LanguagePreference.english
            );
        }
        if (null != fl401Applicant.getSolicitorEmail()) {
            emailService.send(
                fl401Applicant.getSolicitorEmail(),
                EmailTemplateNames.APPLICANT_SOLICITOR_HEARING_DETAILS,
                buildApplicantOrRespondentSolicitorHearingEmail(caseData, hearingRequest, applicantSolicitorName),
                LanguagePreference.english
            );
        }
    }

    private void sendHearingDetailsEmailForCA(CaseData caseData, HearingRequest hearingRequest) {
        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> applicantsEmailList = applicants.stream()
            .map(PartyDetails::getEmail)
            .collect(Collectors.toList());

        if (!applicantsEmailList.isEmpty() && !applicantsEmailList.contains(null)) {
            for (String email : applicantsEmailList) {
                Optional<String> partyName = applicants.stream()
                    .filter(p -> p.getEmail().equals(email))
                    .map(element -> element.getFirstName() + " " + element.getLastName())
                    .findAny();
                emailService.send(
                    email,
                    EmailTemplateNames.HEARING_DETAILS,
                    buildApplicantOrRespondentEmail(caseData, partyName.orElse(null)),
                    LanguagePreference.english
                );
            }
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

        if (!respondentsEmailList.isEmpty() && !respondentsEmailList.contains(null)) {
            for (String email : respondentsEmailList) {
                Optional<String> partyName = respondents.stream()
                    .filter(p -> p.getEmail().equals(email))
                    .map(element -> element.getFirstName() + " " + element.getLastName())
                    .findAny();
                emailService.send(
                    email,
                    EmailTemplateNames.HEARING_DETAILS,
                    buildApplicantOrRespondentEmail(caseData, partyName.orElse(null)),
                    LanguagePreference.english
                );
            }
        }
        sendHearingDetailsEmailForCaSolicitor(caseData, hearingRequest, applicants, respondents);
    }

    private void sendHearingDetailsEmailForCaSolicitor(CaseData caseData, HearingRequest hearingRequest,
                                                       List<PartyDetails> applicants, List<PartyDetails> respondents) {
        List<String> applicantSolicitorsEmailList = applicants.stream()
            .map(PartyDetails::getSolicitorEmail)
            .collect(Collectors.toList());

        for (String applicantSolicitorEmail : applicantSolicitorsEmailList) {
            Optional<String> solicitorName = applicants.stream()
                .filter(p -> p.getSolicitorEmail().equals(applicantSolicitorEmail))
                .map(element -> element.getRepresentativeFirstName() + " " + element.getRepresentativeLastName())
                .findAny();
            emailService.send(
                applicantSolicitorEmail,
                EmailTemplateNames.APPLICANT_SOLICITOR_HEARING_DETAILS,
                buildApplicantOrRespondentSolicitorHearingEmail(caseData, hearingRequest, solicitorName.orElse(null)),
                LanguagePreference.english
            );
        }

        List<String> respondentSolicitorsEmailList = respondents.stream()
            .map(PartyDetails::getSolicitorEmail)
            .collect(Collectors.toList());

        if (!respondentSolicitorsEmailList.isEmpty() && !respondentSolicitorsEmailList.contains(null)) {
            for (String respondentSolicitorEmail : respondentSolicitorsEmailList) {
                Optional<String> solicitorName = respondents.stream()
                    .filter(p -> p.getSolicitorEmail().equals(respondentSolicitorEmail))
                    .map(element -> element.getRepresentativeFirstName() + " " + element.getRepresentativeLastName())
                    .findAny();
                respondentSolicitorsEmailList.forEach(email -> emailService.send(
                    email,
                    EmailTemplateNames.RESPONDENT_SOLICITOR_HEARING_DETAILS,
                    buildApplicantOrRespondentSolicitorHearingEmail(
                        caseData,
                        hearingRequest,
                        solicitorName.orElse(null)
                    ),
                    LanguagePreference.english
                ));
            }
        }
    }

    private EmailTemplateVars buildApplicantOrRespondentSolicitorHearingEmail(CaseData caseData, HearingRequest hearingRequest,
                                                                         String applicantSolicitorName) {

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        return HearingDetailsEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .issueDate(String.valueOf(issueDate.format(dateTimeFormatter)))
            .typeOfHearing(" ")
            .hearingDateAndTime(String.valueOf(hearingRequest.getHearingUpdate().getNextHearingDate()))
            .hearingVenue(hearingRequest.getHearingUpdate().getHearingVenueName())
            .partySolicitorName(applicantSolicitorName)
            .build();
    }

    private EmailTemplateVars buildApplicantOrRespondentEmail(CaseData caseData, String partyName) {

        return HearingDetailsEmail.builder()
                .caseReference(String.valueOf(caseData.getId()))
                .caseName(caseData.getApplicantCaseName())
                .partyName(partyName)
                .hearingDetailsPageLink(dashboardUrl + "/dashboard")
                .build();
    }

}
