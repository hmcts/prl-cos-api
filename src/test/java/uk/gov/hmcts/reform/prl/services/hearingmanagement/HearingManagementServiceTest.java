package uk.gov.hmcts.reform.prl.services.hearingmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public static final String HEARING_STATE_CHANGE_SUCCESS = "hmcCaseUpdateSuccess";
    public static final String HEARING_STATE_CHANGE_FAILURE = "hmcCaseUpdateFailure";
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String authToken = "Bearer TestAuthToken";
    private final String serviceAuthToken = "Bearer testServiceAuth";
    private final String systemUserId = "systemUserID";
    private final String eventToken = "eventToken";
    private String dashBoardUrl = "https://privatelaw.aat.platform.hmcts.net/dashboard";

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
        CaseDetails caseDetails = CaseDetails.builder().id(
            1669565933090179L).data(stringObjectMap).build();
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, hearingRequest.getCaseRef())).thenReturn(caseDetails);
        when(coreCaseDataApi.startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                     caseType, hearingRequest.getCaseRef(), "hmcCaseUpdateSuccess"))
            .thenReturn(buildStartEventResponse("hmcCaseUpdateSuccess", eventToken));
        when(coreCaseDataApi.submitEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                      caseType, hearingRequest.getCaseRef(), true,
                                                      buildCaseDataContent("hmcCaseUpdateSuccess", eventToken,
                                                                           c100CaseData.getState())))
            .thenReturn(caseDetails);

        doNothing().when(allTabService).updateAllTabsIncludingConfTab(c100CaseData);

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

        verify(coreCaseDataApi).startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                        caseType, hearingRequest.getCaseRef(), "hmcCaseUpdateSuccess"
        );
        verify(coreCaseDataApi).submitEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                         caseType, hearingRequest.getCaseRef(), true,
                                                         buildCaseDataContent("hmcCaseUpdateSuccess", eventToken,
                                                                              c100CaseData.getState())
        );
        assertTrue(true);
    }

    @Test
    public void testHmcStatusAsChangedStateChangeAndNotificationForC100() throws Exception {

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

        c100CaseData = c100CaseData.toBuilder().state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING).build();

        Map<String, Object> stringObjectMap = c100CaseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(c100CaseData);
        CaseDetails caseDetails = CaseDetails.builder().id(
            1669565933090179L).data(stringObjectMap).build();
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, hearingRequest1.getCaseRef())).thenReturn(caseDetails);
        when(coreCaseDataApi.startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                     caseType, hearingRequest1.getCaseRef(), "hmcCaseUpdateFailure"))
            .thenReturn(buildStartEventResponse("hmcCaseUpdateFailure", eventToken));
        when(coreCaseDataApi.submitEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                      caseType, hearingRequest1.getCaseRef(), true,
                                                      buildCaseDataContent("hmcCaseUpdateFailure", eventToken,
                                                                           c100CaseData.getState())))
            .thenReturn(caseDetails);

        doNothing().when(allTabService).updateAllTabsIncludingConfTab(c100CaseData);

        doNothing().when(emailService).send(applicantEmail,
                                            EmailTemplateNames.HEARING_CHANGES,
                                            applicantEmailVars,
                                            LanguagePreference.english);
        doNothing().when(emailService).send(respondentEmail,
                                            EmailTemplateNames.HEARING_CHANGES,
                                            respondentEmailVars,
                                            LanguagePreference.english);

        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest1);

        verify(coreCaseDataApi).startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                        caseType, hearingRequest1.getCaseRef(), "hmcCaseUpdateFailure"
        );
        verify(coreCaseDataApi).submitEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                         caseType, hearingRequest1.getCaseRef(), true,
                                                         buildCaseDataContent("hmcCaseUpdateFailure", eventToken,
                                                                              c100CaseData.getState())
        );

        assertTrue(true);
    }

    @Test
    public void testHmcStatusAsCancelledStateChangeAndNotificationForC100() throws Exception {

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

        c100CaseData = c100CaseData.toBuilder().state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING).build();

        Map<String, Object> stringObjectMap = c100CaseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(c100CaseData);
        CaseDetails caseDetails = CaseDetails.builder().id(
            1669565933090179L).data(stringObjectMap).build();
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, hearingRequest1.getCaseRef())).thenReturn(caseDetails);
        when(coreCaseDataApi.startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                     caseType, hearingRequest1.getCaseRef(), "hmcCaseUpdateFailure"))
            .thenReturn(buildStartEventResponse("hmcCaseUpdateFailure", eventToken));
        when(coreCaseDataApi.submitEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                      caseType, hearingRequest1.getCaseRef(), true,
                                                      buildCaseDataContent("hmcCaseUpdateFailure", eventToken, c100CaseData.getState())))
            .thenReturn(caseDetails);

        doNothing().when(allTabService).updateAllTabsIncludingConfTab(c100CaseData);

        doNothing().when(emailService).send(applicantEmail,
                                            EmailTemplateNames.HEARING_CANCELLED,
                                            applicantEmailVars,
                                            LanguagePreference.english);
        doNothing().when(emailService).send(respondentEmail,
                                            EmailTemplateNames.HEARING_CANCELLED,
                                            respondentEmailVars,
                                            LanguagePreference.english);

        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest1);

        verify(coreCaseDataApi).startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                        caseType, hearingRequest1.getCaseRef(), "hmcCaseUpdateFailure"
        );
        verify(coreCaseDataApi).submitEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                         caseType, hearingRequest1.getCaseRef(), true,
                                                         buildCaseDataContent("hmcCaseUpdateFailure", eventToken,
                                                                              c100CaseData.getState())
        );

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

    private StartEventResponse buildStartEventResponse(String eventId, String eventToken) {
        return StartEventResponse.builder().eventId(eventId).token(eventToken).build();
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

        Map<String, Object> stringObjectMap = c100CaseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(fl401CaseData);
        CaseDetails caseDetails = CaseDetails.builder().id(
            1669565933090179L).data(stringObjectMap).build();
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, hearingRequest1.getCaseRef())).thenReturn(caseDetails);
        when(coreCaseDataApi.startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                     caseType, hearingRequest1.getCaseRef(), "hmcCaseUpdateSuccess"))
            .thenReturn(buildStartEventResponse("hmcCaseUpdateSuccess", eventToken));
        when(coreCaseDataApi.submitEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                      caseType, hearingRequest1.getCaseRef(), true,
                                                      buildCaseDataContent("hmcCaseUpdateSuccess", eventToken, fl401CaseData.getState())))
            .thenReturn(caseDetails);

        doNothing().when(allTabService).updateAllTabsIncludingConfTab(fl401CaseData);

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

        verify(coreCaseDataApi).startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                        caseType, hearingRequest1.getCaseRef(), "hmcCaseUpdateSuccess"
        );
        verify(coreCaseDataApi).submitEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                         caseType, hearingRequest1.getCaseRef(), true,
                                                         buildCaseDataContent("hmcCaseUpdateSuccess", eventToken, fl401CaseData.getState())
        );
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
        CaseDetails caseDetails = CaseDetails.builder().id(
            1669565933090179L).data(stringObjectMap).build();

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

        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, hearingRequest1.getCaseRef())).thenReturn(caseDetails);
        when(coreCaseDataApi.startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                     caseType, hearingRequest1.getCaseRef(), "hmcCaseUpdateFailure"))
            .thenReturn(buildStartEventResponse("hmcCaseUpdateFailure", eventToken));
        when(coreCaseDataApi.submitEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                      caseType, hearingRequest1.getCaseRef(), true,
                                                      buildCaseDataContent("hmcCaseUpdateFailure", eventToken, fl401CaseData.getState())))
            .thenReturn(caseDetails);

        doNothing().when(allTabService).updateAllTabsIncludingConfTab(fl401CaseData);

        doNothing().when(emailService).send(applicantEmail,
                                            EmailTemplateNames.HEARING_CHANGES,
                                            applicantEmailVars,
                                            LanguagePreference.english);
        doNothing().when(emailService).send(respondentEmail,
                                            EmailTemplateNames.HEARING_CHANGES,
                                            respondentEmailVars,
                                            LanguagePreference.english);

        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest1);

        verify(coreCaseDataApi).startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                        caseType, hearingRequest1.getCaseRef(), "hmcCaseUpdateFailure"
        );
        verify(coreCaseDataApi).submitEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                         caseType, hearingRequest1.getCaseRef(), true,
                                                         buildCaseDataContent("hmcCaseUpdateFailure", eventToken,
                                                                              fl401CaseData.getState())
        );

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

        Map<String, Object> stringObjectMap = c100CaseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(fl401CaseData);
        CaseDetails caseDetails = CaseDetails.builder().id(
            1669565933090179L).data(stringObjectMap).build();

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
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, hearingRequest1.getCaseRef())).thenReturn(caseDetails);
        when(coreCaseDataApi.startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                     caseType, hearingRequest1.getCaseRef(), "hmcCaseUpdateFailure"))
            .thenReturn(buildStartEventResponse("hmcCaseUpdateFailure", eventToken));
        when(coreCaseDataApi.submitEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                      caseType, hearingRequest1.getCaseRef(), true,
                                                      buildCaseDataContent("hmcCaseUpdateFailure", eventToken,
                                                                           fl401CaseData.getState())))
            .thenReturn(caseDetails);

        doNothing().when(allTabService).updateAllTabsIncludingConfTab(fl401CaseData);

        doNothing().when(emailService).send(applicantEmail,
                                            EmailTemplateNames.HEARING_CANCELLED,
                                            applicantEmailVars,
                                            LanguagePreference.english);
        doNothing().when(emailService).send(respondentEmail,
                                            EmailTemplateNames.HEARING_CANCELLED,
                                            respondentEmailVars,
                                            LanguagePreference.english);

        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest1);

        verify(coreCaseDataApi).startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                        caseType, hearingRequest1.getCaseRef(), "hmcCaseUpdateFailure"
        );
        verify(coreCaseDataApi).submitEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                         caseType, hearingRequest1.getCaseRef(), true,
                                                         buildCaseDataContent("hmcCaseUpdateFailure", eventToken,
                                                                              fl401CaseData.getState())
        );
        assertTrue(true);
    }
}
