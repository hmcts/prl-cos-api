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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingsUpdate;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDateRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.HearingDetailsEmail;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.State.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.prl.enums.State.PREPARE_FOR_HEARING_CONDUCT_HEARING;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HearingManagementServiceTest {

    @InjectMocks
    private HearingManagementService hearingManagementService;

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
    private CcdCoreCaseDataService ccdCoreCaseDataService;

    private HearingRequest hearingRequest;

    private NextHearingDateRequest nextHearingDateRequest;

    private CaseData c100CaseData;
    private HearingDetailsEmail applicantEmailVars;
    private HearingDetailsEmail respondentEmailVars;
    private HearingDetailsEmail applicantSolicitorEmailvars;
    private HearingDetailsEmail respondentSolicitorEmailvars;
    private String respondentEmail;
    private String applicantEmail;
    private String respondentSolicitorEmail;
    private String applicantSolicitorEmail;
    private Map<String, Object> stringObjectMap;
    private CaseDetails caseDetails;
    private StartEventResponse startEventResponse;

    private final String jurisdiction = "PRIVATELAW";
    private final String caseType = "PRLAPPS";

    public static final String HMC_CASE_STATUS_UPDATE_TO_DECISION_OUTCOME = "hmcCaseUpdDecOutcome";
    public static final String HMC_CASE_STATUS_UPDATE_TO_PREP_FOR_HEARING = "hmcCaseUpdPrepForHearing";
    public static final String UPDATE_NEXT_HEARING_DATE_IN_CCD = "updateNextHearingInfo";

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String authToken = "Bearer TestAuthToken";
    private final String serviceAuthToken = "Bearer testServiceAuth";
    private final String systemUserId = "systemUserID";
    private final String eventToken = "eventToken";
    private String dashBoardUrl = "https://privatelaw.aat.platform.hmcts.net/dashboard";

    private LocalDateTime testNextHearingDate = LocalDateTime.of(2024, 04, 28, 1, 0);

    @Before
    public void setup() {

        nextHearingDateRequest = NextHearingDateRequest.builder()
            .caseRef("1669565933090179")
            .nextHearingDetails(NextHearingDetails.builder().hearingID("123").hearingDateTime(testNextHearingDate)
                                    .build())
            .build();

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
            .nextHearingDateRequest(nextHearingDateRequest)
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
            .state(DECISION_OUTCOME)
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
        stringObjectMap = new HashMap<>();
        stringObjectMap.put("id", "1233456787678");
        caseDetails = CaseDetails.builder().id(12345L).data(stringObjectMap).build();
        startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(authToken).build();
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(systemUserService.getUserId(authToken)).thenReturn(systemUserId);
        when(systemUserService.getSysUserToken()).thenReturn(authToken);
        when(ccdCoreCaseDataService.submitUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(caseDetails);
        when(ccdCoreCaseDataService.startUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(startEventResponse);
        when(ccdCoreCaseDataService.submitCreate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyBoolean()
        )).thenReturn(caseDetails);
        when(ccdCoreCaseDataService.startSubmitCreate(Mockito.anyString(),
                                                      Mockito.anyString(),
                                                      Mockito.any(),
                                                      Mockito.anyBoolean())).thenReturn(startEventResponse);

    }

    @Test
    public void testHmcStateAsListedAndStateChangeAndNotificationForC100() throws Exception {
        caseDetails = caseDetails.toBuilder().data(stringObjectMap).build();
        when(objectMapper.convertValue(stringObjectMap,CaseData.class)).thenReturn(c100CaseData);

        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest,DECISION_OUTCOME);

        verify(ccdCoreCaseDataService, times(2)).startUpdate(Mockito.anyString(),
                                                             Mockito.any(),
                                                             Mockito.anyString(),
                                                             Mockito.anyBoolean()
        );
        verify(ccdCoreCaseDataService, times(1)).submitUpdate(Mockito.anyString(), Mockito.any(),
                                                              Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());

        assertTrue(true);
    }

    @Test
    public void testHmcStatusAsChangedStateChangeAndNotificationForC100() throws Exception {
        c100CaseData = c100CaseData.toBuilder().state(PREPARE_FOR_HEARING_CONDUCT_HEARING).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(c100CaseData);

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
            .nextHearingDateRequest(nextHearingDateRequest)
            .build();
        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest1,DECISION_OUTCOME);

        verify(ccdCoreCaseDataService, times(2)).startUpdate(Mockito.anyString(),
                                                             Mockito.any(),
                                                             Mockito.anyString(),
                                                             Mockito.anyBoolean()
        );
        verify(ccdCoreCaseDataService, times(1)).submitUpdate(Mockito.anyString(), Mockito.any(),
                                                              Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());

        assertTrue(true);
    }

    @Test
    public void testHmcStatusAsCancelledStateChangeAndNotificationForC100() throws Exception {
        c100CaseData = c100CaseData.toBuilder().state(PREPARE_FOR_HEARING_CONDUCT_HEARING).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(c100CaseData);

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
            .nextHearingDateRequest(nextHearingDateRequest)
            .build();
        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest1, DECISION_OUTCOME);

        verify(ccdCoreCaseDataService, times(2)).startUpdate(Mockito.anyString(),
                                                             Mockito.any(),
                                                             Mockito.anyString(),
                                                             Mockito.anyBoolean()
        );
        verify(ccdCoreCaseDataService, times(1)).submitUpdate(Mockito.anyString(), Mockito.any(),
                                                              Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());

        assertTrue(true);
    }

    @Test
    public void testHmcStateAsListedAndStateChangeAndNotificationForFl401() throws Exception {
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
            .nextHearingDateRequest(nextHearingDateRequest)
            .build();
        applicantSolicitorEmailvars = HearingDetailsEmail.builder()
            .caseReference(String.valueOf(fl401CaseData.getId()))
            .caseName(fl401CaseData.getApplicantCaseName())
            .issueDate(issueDate.format(dateTimeFormatter))
            .typeOfHearing(" ")
            .hearingDateAndTime(String.valueOf(hearingRequest1.getHearingUpdate().getNextHearingDate()))
            .hearingVenue(hearingRequest1.getHearingUpdate().getHearingVenueName())
            .partySolicitorName(applicantFl401.getRepresentativeFirstName() + " " + applicantFl401.getRepresentativeLastName())
            .build();

        applicantEmail = applicantFl401.getEmail();
        applicantSolicitorEmail = applicantFl401.getSolicitorEmail();
        respondentEmail = respondentFl401.getEmail();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(fl401CaseData);

        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest1, DECISION_OUTCOME);

        verify(ccdCoreCaseDataService, times(2)).startUpdate(Mockito.anyString(),
                                                             Mockito.any(),
                                                             Mockito.anyString(),
                                                             Mockito.anyBoolean()
        );
        verify(ccdCoreCaseDataService, times(1)).submitUpdate(Mockito.anyString(), Mockito.any(),
                                                              Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());

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
            .state(PREPARE_FOR_HEARING_CONDUCT_HEARING)
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

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(fl401CaseData);

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
            .nextHearingDateRequest(nextHearingDateRequest)
            .build();
        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest1, DECISION_OUTCOME);

        verify(ccdCoreCaseDataService, times(2)).startUpdate(Mockito.anyString(),
                                                             Mockito.any(),
                                                             Mockito.anyString(),
                                                             Mockito.anyBoolean()
        );
        verify(ccdCoreCaseDataService, times(1)).submitUpdate(Mockito.anyString(), Mockito.any(),
                                                              Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());

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
            .state(PREPARE_FOR_HEARING_CONDUCT_HEARING)
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
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(fl401CaseData);

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
            .nextHearingDateRequest(nextHearingDateRequest)
            .build();
        hearingManagementService.caseStateChangeForHearingManagement(hearingRequest1, DECISION_OUTCOME);

        verify(ccdCoreCaseDataService, times(2)).startUpdate(Mockito.anyString(),
                                                             Mockito.any(),
                                                             Mockito.anyString(),
                                                             Mockito.anyBoolean()
        );
        verify(ccdCoreCaseDataService, times(1)).submitUpdate(Mockito.anyString(), Mockito.any(),
                                                              Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());

        assertTrue(true);
    }

    @Test
    public void testHmcNextHearingDateChangeAndNotificationForC100() throws Exception {
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(c100CaseData);

        hearingManagementService.caseNextHearingDateChangeForHearingManagement(nextHearingDateRequest);

        verify(ccdCoreCaseDataService, times(1)).startUpdate(Mockito.anyString(),
                                                             Mockito.any(),
                                                             Mockito.anyString(),
                                                             Mockito.anyBoolean()
        );
        verify(ccdCoreCaseDataService, times(1)).submitUpdate(Mockito.anyString(), Mockito.any(),
                                                              Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());


        assertTrue(true);
    }

}
