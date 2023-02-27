package uk.gov.hmcts.reform.prl.services.hearingmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingsUpdate;
import uk.gov.hmcts.reform.prl.models.dto.notify.HearingDetailsEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HearingManagementServiceTest {

    @InjectMocks
    private HearingManagementService hearingManagementService;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private AllTabServiceImpl allTabService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private EmailService emailService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    private StartEventResponse startEventResponse;

    private CaseDetails caseDetails;

    private CaseDataContent caseDataContent;
    private HearingRequest hearingRequest;
    private CaseData c100CaseData;
    private HearingDetailsEmail applicantEmailVars;
    private HearingDetailsEmail respondentEmailVars;
    private HearingDetailsEmail applicantSolicitorEmailvars;
    private HearingDetailsEmail respondentSolicitorEmailvars;
    private String respondentEmail;
    private String applicantEmail;
    private String respondentSolicitorEmail;
    private String applicantSolicitorEmail;

    private final String jurisdiction = "PRIVATELAW";
    private final String caseType = "PRLAPPS";
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String authToken = "Bearer TestAuthToken";
    private final String serviceAuthToken = "Bearer testServiceAuth";
    private final String systemUserId = "systemUserID";
    private final String eventToken = "eventToken";
    private String dashBoardUrl = "https://privatelaw.aat.platform.hmcts.net/dashboard";

    private final String userToken = "Bearer testToken";

    private final String systemUpdateUserId = "systemUserID";

    @Before
    public void setup() {
        hearingRequest = HearingRequest.builder()
            .hearingId("123")
            .caseRef("1669565933090179")
            .hearingUpdate(HearingsUpdate.builder()
                               .hearingResponseReceivedDateTime(LocalDate.parse("2022-11-27"))
                               .hearingEventBroadcastDateTime(LocalDate.parse("2022-11-27"))
                               .nextHearingDate(LocalDate.parse("2022-11-27"))
                               .hearingVenueId("MRD-CRT-0817")
                               .hearingVenueName("Aldershot")
                               .hmcStatus("LISTED")
                               .build())
            .build();

        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("test_applicant@gmail.com")
            .representativeFirstName("applicant Solicitor1")
            .representativeLastName("test1")
            .solicitorEmail("applicant_solicitor@demo.com")
            .build();
        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        PartyDetails respondent = PartyDetails.builder()
            .firstName("RespFirst")
            .lastName("RespLast")
            .email("test_respondent@gmail.com")
            .representativeFirstName("Respondent Solicitor1")
            .representativeLastName("test resp1")
            .solicitorEmail("respondent_solicitor@demo.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .build();
        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        c100CaseData = CaseData.builder()
            .state(State.DECISION_OUTCOME)
            .id(1669565933090179L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicantCaseName("test C100")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .build();

        applicantEmailVars = HearingDetailsEmail.builder()
            .caseReference(String.valueOf(c100CaseData.getId()))
            .caseName(c100CaseData.getApplicantCaseName())
            .partyName(applicant.getFirstName() + " " + applicant.getLastName())
            .hearingDetailsPageLink(dashBoardUrl)
            .build();

        respondentEmailVars = HearingDetailsEmail.builder()
            .caseReference(String.valueOf(c100CaseData.getId()))
            .caseName(c100CaseData.getApplicantCaseName())
            .partyName(respondent.getFirstName() + " " + respondent.getLastName())
            .hearingDetailsPageLink(dashBoardUrl)
            .build();

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        applicantSolicitorEmailvars = HearingDetailsEmail.builder()
            .caseReference(String.valueOf(c100CaseData.getId()))
            .caseName(c100CaseData.getApplicantCaseName())
            .issueDate(String.valueOf(issueDate.format(dateTimeFormatter)))
            .typeOfHearing(" ")
            .hearingDateAndTime(String.valueOf(hearingRequest.getHearingUpdate().getNextHearingDate()))
            .hearingVenue(hearingRequest.getHearingUpdate().getHearingVenueName())
            .partySolicitorName(applicant.getRepresentativeFirstName() + " " + applicant.getRepresentativeLastName())
            .build();

        respondentSolicitorEmailvars = HearingDetailsEmail.builder()
            .caseReference(String.valueOf(c100CaseData.getId()))
            .caseName(c100CaseData.getApplicantCaseName())
            .issueDate(String.valueOf(issueDate.format(dateTimeFormatter)))
            .typeOfHearing(" ")
            .hearingDateAndTime(String.valueOf(hearingRequest.getHearingUpdate().getNextHearingDate()))
            .hearingVenue(hearingRequest.getHearingUpdate().getHearingVenueName())
            .partySolicitorName(respondent.getRepresentativeFirstName() + " " + respondent.getRepresentativeLastName())
            .build();

        applicantEmail = applicant.getEmail();
        applicantSolicitorEmail = applicant.getSolicitorEmail();
        respondentEmail = respondent.getEmail();
        respondentSolicitorEmail = respondent.getSolicitorEmail();

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(systemUserService.getUserId(authToken)).thenReturn(systemUserId);
        when(systemUserService.getSysUserToken()).thenReturn(authToken);

    }

    @Test
    public void testHmcStateAsListedAndStateChangeAndNotificationForC100() throws Exception {
        Map<String, Object> stringObjectMap = c100CaseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(c100CaseData);
        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        when(systemUserService.getUserId(userToken)).thenReturn(systemUpdateUserId);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1669565933090179L)
            .data(stringObjectMap)
            .state(State.CASE_ISSUE.getValue())
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .build();

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(stringObjectMap)
            .build();

        EventRequestData eventRequestData = EventRequestData.builder()
            .eventId(CaseEvent.HEARING_STATE_CHANGE_SUCCESS.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUpdateUserId)
            .userToken(userToken)
            .build();
       startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(userToken).build();
        CaseData caseDataUpdated = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);
        when(coreCaseDataService.startUpdate(
            userToken,eventRequestData, hearingRequest.getCaseRef(),true))
            .thenReturn(startEventResponse);

        EventRequestData allTabsUpdateEventRequestData = EventRequestData.builder()
            .eventId(CaseEvent.UPDATE_ALL_TABS.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUpdateUserId)
            .userToken(userToken)
            .build();
        StartEventResponse allTabsUpdateStartEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(userToken).build();
        CaseData caseDataUpdatedforAllTabs = CaseUtils.getCaseDataFromStartUpdateEventResponse(allTabsUpdateStartEventResponse, objectMapper);
        when(coreCaseDataService.startUpdate(
            userToken,allTabsUpdateEventRequestData, hearingRequest.getCaseRef(),true))
            .thenReturn(allTabsUpdateStartEventResponse);

        when(coreCaseDataService.eventRequest(CaseEvent.HEARING_STATE_CHANGE_SUCCESS, systemUpdateUserId)).thenReturn(eventRequestData);
        when(coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, systemUpdateUserId)).thenReturn(allTabsUpdateEventRequestData);

        when(coreCaseDataService.createCaseDataContent(startEventResponse,caseDataUpdated)).thenReturn(caseDataContent);
        when(coreCaseDataService.submitUpdate(userToken, eventRequestData, caseDataContent,hearingRequest.getCaseRef(), true))
            .thenReturn(caseDetails);

        doNothing().when(allTabService).updateAllTabsIncludingConfTabRefactored(userToken,
                                                                                hearingRequest.getCaseRef(),
                                                                                allTabsUpdateStartEventResponse,
                                                                                allTabsUpdateEventRequestData,
                                                                                c100CaseData);

        doNothing().when(emailService).send(applicantEmail,
                                            EmailTemplateNames.HEARING_DETAILS,
                                            applicantEmailVars,
                                            LanguagePreference.english);
        doNothing().when(emailService).send(respondentEmail,
                                            EmailTemplateNames.HEARING_DETAILS,
                                            respondentEmailVars,
                                            LanguagePreference.english);

        doNothing().when(emailService).send(applicantSolicitorEmail,
                                            EmailTemplateNames.APPLICANT_SOLICITOR_HEARING_DETAILS,
                                            applicantSolicitorEmailvars,
                                            LanguagePreference.english);

        doNothing().when(emailService).send(respondentSolicitorEmail,
                                            EmailTemplateNames.RESPONDENT_SOLICITOR_HEARING_DETAILS,
                                            respondentSolicitorEmailvars,
                                            LanguagePreference.english);

        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest);

        verify(coreCaseDataService).startUpdate(userToken,
                                                eventRequestData,
                                                hearingRequest.getCaseRef(),
                                                true);
        verify(coreCaseDataService).submitUpdate(userToken,
                                                 eventRequestData,
                                                 caseDataContent,
                                                 hearingRequest.getCaseRef(),
                                                 true);
        assertTrue(true);
    }

    @Test
    public void testHmcStatusAsChangedStateChangeAndNotificationForC100() throws Exception {
        c100CaseData = c100CaseData.toBuilder().state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING).build();
        Map<String, Object> stringObjectMap = c100CaseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(c100CaseData);
        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        when(systemUserService.getUserId(userToken)).thenReturn(systemUpdateUserId);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1669565933090179L)
            .data(stringObjectMap)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue())
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .build();

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(stringObjectMap)
            .build();

        EventRequestData eventRequestData = EventRequestData.builder()
            .eventId(CaseEvent.HEARING_STATE_CHANGE_FAILURE.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUpdateUserId)
            .userToken(userToken)
            .build();
         startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(userToken).build();
        CaseData caseDataUpdated = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);

        HearingRequest hearingRequest1 = HearingRequest.builder().hearingId("123").caseRef("1669565933090179")
            .hearingUpdate(HearingsUpdate.builder().hearingResponseReceivedDateTime(LocalDate.parse("2022-11-27"))
                               .hearingEventBroadcastDateTime(LocalDate.parse("2022-11-27"))
                               .nextHearingDate(LocalDate.parse("2022-11-27")).hearingVenueId("MRD-CRT-0817")
                               .hearingVenueName("Aldershot").hmcStatus("ADJOURNED").build()).build();
        when(coreCaseDataService.startUpdate(userToken,eventRequestData, hearingRequest1.getCaseRef(),true))
            .thenReturn(startEventResponse);

        CaseEvent caseEvent = CaseEvent.HEARING_STATE_CHANGE_FAILURE;
        EventRequestData allTabsUpdateEventRequestData = EventRequestData.builder()
            .eventId(caseEvent.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUpdateUserId)
            .userToken(userToken)
            .build();
        StartEventResponse allTabsUpdateStartEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(userToken).build();
        CaseData caseDataUpdatedforAllTabs = CaseUtils.getCaseDataFromStartUpdateEventResponse(allTabsUpdateStartEventResponse, objectMapper);
        when(coreCaseDataService.startUpdate(
            userToken,allTabsUpdateEventRequestData, hearingRequest1.getCaseRef(),true))
            .thenReturn(allTabsUpdateStartEventResponse);

        when(coreCaseDataService.eventRequest(caseEvent, systemUpdateUserId)).thenReturn(eventRequestData);
        when(coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, systemUpdateUserId)).thenReturn(allTabsUpdateEventRequestData);

        when(coreCaseDataService.createCaseDataContent(startEventResponse,caseDataUpdated)).thenReturn(caseDataContent);
        when(coreCaseDataService.submitUpdate(userToken, eventRequestData, caseDataContent,hearingRequest1.getCaseRef(), true))
            .thenReturn(caseDetails);

        doNothing().when(allTabService).updateAllTabsIncludingConfTabRefactored(userToken,
                                                                                hearingRequest1.getCaseRef(),
                                                                                allTabsUpdateStartEventResponse,
                                                                                allTabsUpdateEventRequestData,
                                                                                c100CaseData);

        doNothing().when(emailService).send(applicantEmail,
                                            EmailTemplateNames.HEARING_CHANGES,
                                            applicantEmailVars,
                                            LanguagePreference.english);
        doNothing().when(emailService).send(respondentEmail,
                                            EmailTemplateNames.HEARING_CHANGES,
                                            respondentEmailVars,
                                            LanguagePreference.english);

        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest1);

        verify(coreCaseDataService, Mockito.times(2)).startUpdate(userToken,
                                                eventRequestData,
                                                hearingRequest1.getCaseRef(),
                                                true);
        verify(coreCaseDataService, Mockito.times(1)).submitUpdate(userToken,
                                                                   eventRequestData,
                                                                   caseDataContent,
                                                                   hearingRequest1.getCaseRef(),
                                                                   true);
        assertTrue(true);
    }

    @Test
    public void testHmcStatusAsCancelledStateChangeAndNotificationForC100() throws Exception {
        hearingRequest = HearingRequest.builder()
            .hearingId("123")
            .caseRef("1669565933090179")
            .hearingUpdate(HearingsUpdate.builder()
                               .hearingResponseReceivedDateTime(LocalDate.parse("2022-11-27"))
                               .hearingEventBroadcastDateTime(LocalDate.parse("2022-11-27"))
                               .nextHearingDate(LocalDate.parse("2022-11-27"))
                               .hearingVenueId("MRD-CRT-0817")
                               .hearingVenueName("Aldershot")
                               .hmcStatus("CANCELLED")
                               .build())
            .build();

        c100CaseData = c100CaseData.toBuilder().state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING).build();
        Map<String, Object> stringObjectMap = c100CaseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(c100CaseData);
        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        when(systemUserService.getUserId(userToken)).thenReturn(systemUpdateUserId);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1669565933090179L)
            .data(stringObjectMap)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue())
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .build();

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(stringObjectMap)
            .build();

        EventRequestData eventRequestData = EventRequestData.builder()
            .eventId(CaseEvent.HEARING_STATE_CHANGE_FAILURE.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUpdateUserId)
            .userToken(userToken)
            .build();
         startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(userToken).build();
        CaseData caseDataUpdated = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);
        when(coreCaseDataService.startUpdate(
            userToken,eventRequestData, hearingRequest.getCaseRef(),true))
            .thenReturn(startEventResponse);

        CaseEvent caseEvent  = CaseEvent.HEARING_STATE_CHANGE_FAILURE;
        EventRequestData allTabsUpdateEventRequestData = EventRequestData.builder()
            .eventId(caseEvent.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUpdateUserId)
            .userToken(userToken)
            .build();
        StartEventResponse allTabsUpdateStartEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(userToken).build();
        CaseData caseDataUpdatedforAllTabs = CaseUtils.getCaseDataFromStartUpdateEventResponse(allTabsUpdateStartEventResponse, objectMapper);
        when(coreCaseDataService.startUpdate(
            userToken,allTabsUpdateEventRequestData, hearingRequest.getCaseRef(),true))
            .thenReturn(allTabsUpdateStartEventResponse);

        when(coreCaseDataService.eventRequest(caseEvent, systemUpdateUserId)).thenReturn(eventRequestData);
        when(coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, systemUpdateUserId)).thenReturn(allTabsUpdateEventRequestData);

        when(coreCaseDataService.createCaseDataContent(startEventResponse,caseDataUpdated)).thenReturn(caseDataContent);
        when(coreCaseDataService.submitUpdate(userToken, eventRequestData, caseDataContent,hearingRequest.getCaseRef(), true))
            .thenReturn(caseDetails);

        doNothing().when(allTabService).updateAllTabsIncludingConfTabRefactored(userToken,
                                                                                hearingRequest.getCaseRef(),
                                                                                allTabsUpdateStartEventResponse,
                                                                                allTabsUpdateEventRequestData,
                                                                                c100CaseData);


        doNothing().when(allTabService).updateAllTabsIncludingConfTab(c100CaseData);

        doNothing().when(emailService).send(applicantEmail,
                                            EmailTemplateNames.HEARING_CANCELLED,
                                            applicantEmailVars,
                                            LanguagePreference.english);
        doNothing().when(emailService).send(respondentEmail,
                                            EmailTemplateNames.HEARING_CANCELLED,
                                            respondentEmailVars,
                                            LanguagePreference.english);


        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest);

        verify(coreCaseDataService, Mockito.times(2)).startUpdate(userToken,
                                                                  eventRequestData,
                                                                  hearingRequest.getCaseRef(),
                                                                  true);
        verify(coreCaseDataService, Mockito.times(1)).submitUpdate(userToken,
                                                                   eventRequestData,
                                                                   caseDataContent,
                                                                   hearingRequest.getCaseRef(),
                                                                   true);

        assertTrue(true);
    }

    private CaseDataContent buildCaseDataContent(String eventId, String eventToken, State state) {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("state", state);

        return CaseDataContent.builder()
            .eventToken(eventToken)
            .event(Event.builder()
                       .id(eventId)
                       .build())
            .data(caseDataMap)
            .build();
    }

    @Test
    public void testHmcStateAsListedAndStateChangeAndNotificationForFl401() throws Exception {
        HearingRequest hearingRequest1 = HearingRequest.builder()
            .hearingId("123")
            .caseRef("1669565933090179")
            .hearingUpdate(HearingsUpdate.builder()
                               .hearingResponseReceivedDateTime(LocalDate.parse("2022-11-27"))
                               .hearingEventBroadcastDateTime(LocalDate.parse("2022-11-27"))
                               .nextHearingDate(LocalDate.parse("2022-11-27"))
                               .hearingVenueId("MRD-CRT-0817")
                               .hearingVenueName("Aldershot")
                               .hmcStatus("LISTED")
                               .build())
            .build();

        PartyDetails applicantFl401 = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("test_applicant@gmail.com")
            .representativeFirstName("applicant Solicitor1")
            .representativeLastName("test1")
            .solicitorEmail("applicant_solicitor@demo.com")
            .build();

        PartyDetails respondentFl401 = PartyDetails.builder()
            .firstName("RespFirst")
            .lastName("RespLast")
            .email("test_respondent@gmail.com")
            .build();

        CaseData fl401CaseData = CaseData.builder()
            .state(State.DECISION_OUTCOME)

            .id(1669565933090179L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantCaseName("test FL401")
            .applicantsFL401(applicantFl401)
            .respondentsFL401(respondentFl401)
            .build();

        applicantEmailVars = HearingDetailsEmail.builder()
            .caseReference(String.valueOf(fl401CaseData.getId()))
            .caseName(fl401CaseData.getApplicantCaseName())
            .partyName(applicantFl401.getFirstName() + " " + applicantFl401.getLastName())
            .hearingDetailsPageLink(dashBoardUrl)
            .build();

        respondentEmailVars = HearingDetailsEmail.builder()
            .caseReference(String.valueOf(fl401CaseData.getId()))
            .caseName(fl401CaseData.getApplicantCaseName())
            .partyName(respondentFl401.getFirstName() + " " + respondentFl401.getLastName())
            .hearingDetailsPageLink(dashBoardUrl)
            .build();

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        applicantSolicitorEmailvars = HearingDetailsEmail.builder()
            .caseReference(String.valueOf(fl401CaseData.getId()))
            .caseName(fl401CaseData.getApplicantCaseName())
            .issueDate(String.valueOf(issueDate.format(dateTimeFormatter)))
            .typeOfHearing(" ")
            .hearingDateAndTime(String.valueOf(hearingRequest1.getHearingUpdate().getNextHearingDate()))
            .hearingVenue(hearingRequest1.getHearingUpdate().getHearingVenueName())
            .partySolicitorName(applicantFl401.getRepresentativeFirstName() + " " + applicantFl401.getRepresentativeLastName())
            .build();

        applicantEmail = applicantFl401.getEmail();
        applicantSolicitorEmail = applicantFl401.getSolicitorEmail();
        respondentEmail = respondentFl401.getEmail();

        CaseEvent caseEvent = CaseEvent.HEARING_STATE_CHANGE_SUCCESS;
        Map<String, Object> stringObjectMap = c100CaseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(fl401CaseData);
        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        when(systemUserService.getUserId(userToken)).thenReturn(systemUpdateUserId);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1669565933090179L)
            .data(stringObjectMap)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue())
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .build();

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(stringObjectMap)
            .build();

        EventRequestData eventRequestData = EventRequestData.builder()
            .eventId(CaseEvent.HEARING_STATE_CHANGE_FAILURE.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUpdateUserId)
            .userToken(userToken)
            .build();
         startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(userToken).build();
        CaseData caseDataUpdated = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);
        when(coreCaseDataService.startUpdate(
            userToken,eventRequestData, hearingRequest.getCaseRef(),true))
            .thenReturn(startEventResponse);

        EventRequestData allTabsUpdateEventRequestData = EventRequestData.builder()
            .eventId(caseEvent.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUpdateUserId)
            .userToken(userToken)
            .build();
        StartEventResponse allTabsUpdateStartEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(userToken).build();
        CaseData caseDataUpdatedforAllTabs = CaseUtils.getCaseDataFromStartUpdateEventResponse(allTabsUpdateStartEventResponse, objectMapper);
        when(coreCaseDataService.startUpdate(
            userToken,allTabsUpdateEventRequestData, hearingRequest.getCaseRef(),true))
            .thenReturn(allTabsUpdateStartEventResponse);

        when(coreCaseDataService.eventRequest(caseEvent, systemUpdateUserId)).thenReturn(eventRequestData);
        when(coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, systemUpdateUserId)).thenReturn(allTabsUpdateEventRequestData);

        when(coreCaseDataService.createCaseDataContent(startEventResponse,caseDataUpdated)).thenReturn(caseDataContent);
        when(coreCaseDataService.submitUpdate(userToken, eventRequestData, caseDataContent,hearingRequest.getCaseRef(), true))
            .thenReturn(caseDetails);

        doNothing().when(allTabService).updateAllTabsIncludingConfTabRefactored(userToken,
                                                                                hearingRequest.getCaseRef(),
                                                                                allTabsUpdateStartEventResponse,
                                                                                allTabsUpdateEventRequestData,
                                                                                c100CaseData);

        doNothing().when(emailService).send(applicantEmail,
                                            EmailTemplateNames.HEARING_DETAILS,
                                            applicantEmailVars,
                                            LanguagePreference.english);
        doNothing().when(emailService).send(respondentEmail,
                                            EmailTemplateNames.HEARING_DETAILS,
                                            respondentEmailVars,
                                            LanguagePreference.english);
        doNothing().when(emailService).send(applicantSolicitorEmail,
                                            EmailTemplateNames.APPLICANT_SOLICITOR_HEARING_DETAILS,
                                            respondentEmailVars,
                                            LanguagePreference.english);

        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest1);

        verify(coreCaseDataService, Mockito.times(1)).startUpdate(userToken,
                                                                  eventRequestData,
                                                                  hearingRequest.getCaseRef(),
                                                                  true);
        verify(coreCaseDataService, Mockito.times(1)).submitUpdate(userToken,
                                                                   eventRequestData,
                                                                   caseDataContent,
                                                                   hearingRequest.getCaseRef(),
                                                                   true);
        assertTrue(true);
    }


    @Test
    public void testHmcStatusAsChangedStateChangeAndNotificationForFl401() throws Exception {

        PartyDetails applicantFl401 = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("test_applicant@gmail.com")
            .representativeFirstName("applicant Solicitor1")
            .representativeLastName("test1")
            .solicitorEmail("applicant_solicitor@demo.com")
            .build();

        PartyDetails respondentFl401 = PartyDetails.builder()
            .firstName("RespFirst")
            .lastName("RespLast")
            .email("test_respondent@gmail.com")
            .build();

        CaseData fl401CaseData = CaseData.builder()
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .id(1669565933090179L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantCaseName("test FL401")
            .applicantsFL401(applicantFl401)
            .respondentsFL401(respondentFl401)
            .build();

        applicantEmailVars = HearingDetailsEmail.builder()
            .caseReference(String.valueOf(fl401CaseData.getId()))
            .caseName(fl401CaseData.getApplicantCaseName())
            .partyName(applicantFl401.getFirstName() + " " + applicantFl401.getLastName())
            .hearingDetailsPageLink(dashBoardUrl)
            .build();

        respondentEmailVars = HearingDetailsEmail.builder()
            .caseReference(String.valueOf(fl401CaseData.getId()))
            .caseName(fl401CaseData.getApplicantCaseName())
            .partyName(respondentFl401.getFirstName() + " " + respondentFl401.getLastName())
            .hearingDetailsPageLink(dashBoardUrl)
            .build();

        applicantEmail = applicantFl401.getEmail();
        respondentEmail = respondentFl401.getEmail();

        Map<String, Object> stringObjectMap = c100CaseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(fl401CaseData);

        CaseEvent caseEvent = CaseEvent.HEARING_STATE_CHANGE_FAILURE;

        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        when(systemUserService.getUserId(userToken)).thenReturn(systemUpdateUserId);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1669565933090179L)
            .data(stringObjectMap)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue())
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .build();

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(stringObjectMap)
            .build();

        EventRequestData eventRequestData = EventRequestData.builder()
            .eventId(CaseEvent.HEARING_STATE_CHANGE_FAILURE.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUpdateUserId)
            .userToken(userToken)
            .build();
         startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(userToken).build();
        CaseData caseDataUpdated = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);

        HearingRequest hearingRequest1 = HearingRequest.builder()
            .hearingId("123")
            .caseRef("1669565933090179")
            .hearingUpdate(HearingsUpdate.builder()
                               .hearingResponseReceivedDateTime(LocalDate.parse("2022-11-27"))
                               .hearingEventBroadcastDateTime(LocalDate.parse("2022-11-27"))
                               .nextHearingDate(LocalDate.parse("2022-11-27"))
                               .hearingVenueId("MRD-CRT-0817")
                               .hearingVenueName("Aldershot")
                               .hmcStatus("ADJOURNED")
                               .build())
            .build();
        when(coreCaseDataService.startUpdate(
            userToken,eventRequestData, hearingRequest1.getCaseRef(),true))
            .thenReturn(startEventResponse);

        EventRequestData allTabsUpdateEventRequestData = EventRequestData.builder()
            .eventId(caseEvent.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUpdateUserId)
            .userToken(userToken)
            .build();
        StartEventResponse allTabsUpdateStartEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(userToken).build();
        CaseData caseDataUpdatedforAllTabs = CaseUtils.getCaseDataFromStartUpdateEventResponse(allTabsUpdateStartEventResponse, objectMapper);
        when(coreCaseDataService.startUpdate(
            userToken,allTabsUpdateEventRequestData, hearingRequest1.getCaseRef(),true))
            .thenReturn(allTabsUpdateStartEventResponse);

        when(coreCaseDataService.eventRequest(caseEvent, systemUpdateUserId)).thenReturn(eventRequestData);
        when(coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, systemUpdateUserId)).thenReturn(allTabsUpdateEventRequestData);

        when(coreCaseDataService.createCaseDataContent(startEventResponse,caseDataUpdated)).thenReturn(caseDataContent);
        when(coreCaseDataService.submitUpdate(userToken, eventRequestData, caseDataContent,hearingRequest1.getCaseRef(), true))
            .thenReturn(caseDetails);

        doNothing().when(allTabService).updateAllTabsIncludingConfTabRefactored(userToken,
                                                                                hearingRequest1.getCaseRef(),
                                                                                allTabsUpdateStartEventResponse,
                                                                                allTabsUpdateEventRequestData,
                                                                                c100CaseData);

        doNothing().when(emailService).send(applicantEmail,
                                            EmailTemplateNames.HEARING_CHANGES,
                                            applicantEmailVars,
                                            LanguagePreference.english);
        doNothing().when(emailService).send(respondentEmail,
                                            EmailTemplateNames.HEARING_CHANGES,
                                            respondentEmailVars,
                                            LanguagePreference.english);

        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest1);

        verify(coreCaseDataService, Mockito.times(2)).startUpdate(userToken,
                                                                  eventRequestData,
                                                                  hearingRequest1.getCaseRef(),
                                                                  true);
        verify(coreCaseDataService, Mockito.times(1)).submitUpdate(userToken,
                                                                   eventRequestData,
                                                                   caseDataContent,
                                                                   hearingRequest1.getCaseRef(),
                                                                   true);

        assertTrue(true);
    }

    @Test
    public void testHmcStatusAsCancelledStateChangeAndNotificationForFL401() throws Exception {

        PartyDetails applicantFl401 = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("test_applicant@gmail.com")
            .representativeFirstName("applicant Solicitor1")
            .representativeLastName("test1")
            .solicitorEmail("applicant_solicitor@demo.com")
            .build();
        PartyDetails respondentFl401 = PartyDetails.builder()
            .firstName("RespFirst")
            .lastName("RespLast")
            .email("test_respondent@gmail.com")
            .build();

        CaseData fl401CaseData = CaseData.builder()
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .id(1669565933090179L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantCaseName("test FL401")
            .applicantsFL401(applicantFl401)
            .respondentsFL401(respondentFl401)
            .build();

        applicantEmailVars = HearingDetailsEmail.builder()
            .caseReference(String.valueOf(fl401CaseData.getId()))
            .caseName(fl401CaseData.getApplicantCaseName())
            .partyName(applicantFl401.getFirstName() + " " + applicantFl401.getLastName())
            .hearingDetailsPageLink(dashBoardUrl)
            .build();

        respondentEmailVars = HearingDetailsEmail.builder()
            .caseReference(String.valueOf(fl401CaseData.getId()))
            .caseName(fl401CaseData.getApplicantCaseName())
            .partyName(respondentFl401.getFirstName() + " " + respondentFl401.getLastName())
            .hearingDetailsPageLink(dashBoardUrl)
            .build();
        applicantEmail = applicantFl401.getEmail();
        respondentEmail = respondentFl401.getEmail();

        CaseEvent caseEvent = CaseEvent.HEARING_STATE_CHANGE_FAILURE;

        Map<String, Object> stringObjectMap = c100CaseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(fl401CaseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(fl401CaseData);
        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        when(systemUserService.getUserId(userToken)).thenReturn(systemUpdateUserId);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1669565933090179L)
            .data(stringObjectMap)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue())
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .build();

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(stringObjectMap)
            .build();

        EventRequestData eventRequestData = EventRequestData.builder()
            .eventId(CaseEvent.HEARING_STATE_CHANGE_FAILURE.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUpdateUserId)
            .userToken(userToken)
            .build();
         startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(userToken).build();
        CaseData caseDataUpdated = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);

        HearingRequest hearingRequest1 = HearingRequest.builder()
            .hearingId("123")
            .caseRef("1669565933090179")
            .hearingUpdate(HearingsUpdate.builder()
                               .hearingResponseReceivedDateTime(LocalDate.parse("2022-11-27"))
                               .hearingEventBroadcastDateTime(LocalDate.parse("2022-11-27"))
                               .nextHearingDate(LocalDate.parse("2022-11-27"))
                               .hearingVenueId("MRD-CRT-0817")
                               .hearingVenueName("Aldershot")
                               .hmcStatus("CANCELLED")
                               .build())
            .build();
        when(coreCaseDataService.startUpdate(
            userToken,eventRequestData, hearingRequest1.getCaseRef(),true))
            .thenReturn(startEventResponse);

        EventRequestData allTabsUpdateEventRequestData = EventRequestData.builder()
            .eventId(caseEvent.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUpdateUserId)
            .userToken(userToken)
            .build();
        StartEventResponse allTabsUpdateStartEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(userToken).build();
        CaseData caseDataUpdatedforAllTabs = CaseUtils.getCaseDataFromStartUpdateEventResponse(allTabsUpdateStartEventResponse, objectMapper);
        when(coreCaseDataService.startUpdate(
            userToken,allTabsUpdateEventRequestData, hearingRequest1.getCaseRef(),true))
            .thenReturn(allTabsUpdateStartEventResponse);

        when(coreCaseDataService.eventRequest(caseEvent, systemUpdateUserId)).thenReturn(eventRequestData);
        when(coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, systemUpdateUserId)).thenReturn(allTabsUpdateEventRequestData);

        when(coreCaseDataService.createCaseDataContent(startEventResponse,caseDataUpdated)).thenReturn(caseDataContent);
        when(coreCaseDataService.submitUpdate(userToken, eventRequestData, caseDataContent,hearingRequest1.getCaseRef(), true))
            .thenReturn(caseDetails);

        doNothing().when(allTabService).updateAllTabsIncludingConfTabRefactored(userToken,
                                                                                hearingRequest1.getCaseRef(),
                                                                                allTabsUpdateStartEventResponse,
                                                                                allTabsUpdateEventRequestData,
                                                                                c100CaseData);

        doNothing().when(emailService).send(applicantEmail,
                                            EmailTemplateNames.HEARING_CANCELLED,
                                            applicantEmailVars,
                                            LanguagePreference.english);
        doNothing().when(emailService).send(respondentEmail,
                                            EmailTemplateNames.HEARING_CANCELLED,
                                            respondentEmailVars,
                                            LanguagePreference.english);

        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest1);

        verify(coreCaseDataService, Mockito.times(2)).startUpdate(userToken,
                                                                  eventRequestData,
                                                                  hearingRequest1.getCaseRef(),
                                                                  true);
        verify(coreCaseDataService, Mockito.times(1)).submitUpdate(userToken,
                                                                   eventRequestData,
                                                                   caseDataContent,
                                                                   hearingRequest1.getCaseRef(),
                                                                   true);
        assertTrue(true);
    }
}
