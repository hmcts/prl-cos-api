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
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDateRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.HearingDetailsEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NEXT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POSTPONED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WAITING_TO_BE_LISTED;
import static uk.gov.hmcts.reform.prl.enums.State.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.prl.enums.State.PREPARE_FOR_HEARING_CONDUCT_HEARING;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingManagementService {

    public static final String HEARING_STATE_CHANGE_SUCCESS = "hmcCaseUpdateSuccess";

    public static final String HMC_CASE_STATUS_UPDATE_TO_DECISION_OUTCOME = "hmcCaseUpdDecOutcome";

    public static final String HMC_CASE_STATUS_UPDATE_TO_PREP_FOR_HEARING = "hmcCaseUpdPrepForHearing";

    public static final String UPDATE_NEXT_HEARING_DATE_IN_CCD = "updateNextHearingInfo";

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String USER_TOKEN = "userToken";
    public static final String SYSTEM_UPDATE_USER_ID = "systemUpdateUserId";
    public static final String CASE_REF_ID = "id";
    public static final String STATE = "state";
    public static final String EVENT_ID = "eventId";
    public static final String CASE_TYPE_OF_APPLICATION = "caseTypeOfApplication";

    private final AuthorisationService authorisationService;
    private final ObjectMapper objectMapper;
    private final SystemUserService systemUserService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final EmailService emailService;
    private final AllTabServiceImpl allTabService;
    private final CcdCoreCaseDataService coreCaseDataService;

    private final HearingService hearingService;

    @Value("${xui.url}")
    private String manageCaseUrl;

    @Value("${citizen.url}")
    private String dashboardUrl;

    public void caseStateChangeForHearingManagement(HearingRequest hearingRequest, State caseState) throws Exception {

        log.info("Processing the callback for the caseId {} with HMC status {}", hearingRequest.getCaseRef(),
                 hearingRequest.getHearingUpdate().getHmcStatus());

        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);
        log.info("Fetching the Case details based on caseId {}", hearingRequest.getCaseRef()
        );

        Map<String, Object> customFields = new HashMap<>();
        customFields.put(USER_TOKEN, userToken);
        customFields.put(SYSTEM_UPDATE_USER_ID, systemUpdateUserId);
        customFields.put(CASE_REF_ID, hearingRequest.getCaseRef());
        CaseData caseData;
        Map<String, Object> fields = new HashMap<>();
        if (hearingRequest.getNextHearingDateRequest().getNextHearingDetails() != null) {
            fields.put(NEXT_HEARING_DETAILS, hearingRequest.getNextHearingDateRequest().getNextHearingDetails());
        }
        String hmcStatus = hearingRequest.getHearingUpdate().getHmcStatus();
        switch (hmcStatus) {
            case LISTED:
                fields.put(STATE, DECISION_OUTCOME.getValue());
                customFields.put(EVENT_ID, CaseEvent.HEARING_STATE_CHANGE_SUCCESS);
                submitUpdate(fields, customFields);
                caseData = updateTabsWithLatestData(customFields);
                sendHearingDetailsEmail(caseData, hearingRequest);
                break;
            case CANCELLED:
                fields.put(STATE, PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue());
                customFields.put(EVENT_ID, CaseEvent.HEARING_STATE_CHANGE_FAILURE);
                submitUpdate(fields, customFields);
                updateTabsWithLatestData(customFields);
                caseData = updateTabsWithLatestData(customFields);
                sendHearingCancelledEmail(caseData);
                break;
            case WAITING_TO_BE_LISTED:
            case COMPLETED:
            case POSTPONED:
            case ADJOURNED:
                fields.put(STATE, PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue());
                customFields.put(EVENT_ID, CaseEvent.HEARING_STATE_CHANGE_FAILURE);
                submitUpdate(fields, customFields);
                caseData = updateTabsWithLatestData(customFields);
                sendHearingChangeDetailsEmail(caseData);
                break;
            default:
                break;
        }
    }

    public CaseData updateTabsWithLatestData(Map<String, Object> fields) {
        EventRequestData allTabsUpdateEventRequestData = coreCaseDataService.eventRequest(
            CaseEvent.UPDATE_ALL_TABS,
            (String) fields.get(SYSTEM_UPDATE_USER_ID)
        );
        StartEventResponse allTabsUpdateStartEventResponse =
            coreCaseDataService.startUpdate(
                (String) fields.get(USER_TOKEN),
                allTabsUpdateEventRequestData,
                (String) fields.get(CASE_REF_ID),
                true
            );

        CaseData allTabsUpdateCaseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(
            allTabsUpdateStartEventResponse,
            objectMapper
        );
        log.info("Refreshing tab based on the payment response for caseid {} ", fields.get("id"));

        allTabService.updateAllTabsIncludingConfTabRefactored(
            (String) fields.get(USER_TOKEN),
            (String) fields.get(CASE_REF_ID),
            allTabsUpdateStartEventResponse,
            allTabsUpdateEventRequestData,
            allTabsUpdateCaseData
        );
        return allTabsUpdateCaseData;
    }

    private void submitUpdate(Map<String, Object> data, Map<String, Object> fields) {
        CaseEvent caseEvent = CaseEvent.HEARING_STATE_CHANGE_SUCCESS;

        log.info("Following case event will be triggered {}", caseEvent.getValue());

        EventRequestData eventRequestData = coreCaseDataService.eventRequest(
            (CaseEvent) fields.get(EVENT_ID),
            (String) fields.get(SYSTEM_UPDATE_USER_ID)
        );
        StartEventResponse startEventResponse =
            coreCaseDataService.startUpdate(
                (String) fields.get(USER_TOKEN),
                eventRequestData,
                (String) fields.get(CASE_REF_ID),
                true
            );
        CaseData caseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(
            startEventResponse,
            objectMapper
        );

        data.put(CASE_TYPE_OF_APPLICATION, caseData.getCaseTypeOfApplication());

        CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContent(
            startEventResponse,
            data
        );
        coreCaseDataService.submitUpdate(
            (String) fields.get(USER_TOKEN),
            eventRequestData,
            caseDataContent,
            (String) fields.get(CASE_REF_ID),
            true
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

    public void caseNextHearingDateChangeForHearingManagement(NextHearingDateRequest nextHearingDateRequest) throws Exception {

        log.info("Processing the callback for the caseId {} with next hearing date {}", nextHearingDateRequest.getCaseRef(),
                 nextHearingDateRequest.getNextHearingDetails().getHearingDateTime());

        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);
        createEventForNextHearingDate(nextHearingDateRequest, userToken, systemUpdateUserId,
                                      UPDATE_NEXT_HEARING_DATE_IN_CCD);
    }

    private CaseDetails createEventForNextHearingDate(NextHearingDateRequest nextHearingDateRequest, String userToken,
                                                      String systemUpdateUserId, String eventId) {

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            JURISDICTION,
            CASE_TYPE,
            nextHearingDateRequest.getCaseRef(),
            eventId
        );

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("nextHearingDetails", nextHearingDateRequest.getNextHearingDetails());

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(caseDataMap)
            .build();

        return  coreCaseDataApi.submitEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            JURISDICTION,
            CASE_TYPE,
            nextHearingDateRequest.getCaseRef(),
            true,
            caseDataContent
        );
    }

    public NextHearingDetails getNextHearingDate(String caseReference) {
        String userToken = systemUserService.getSysUserToken();
        return hearingService.getNextHearingDate(userToken, caseReference);
    }

}
