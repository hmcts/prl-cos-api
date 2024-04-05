package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javassist.NotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.caseflags.request.CitizenPartyFlagsRequest;
import uk.gov.hmcts.reform.prl.models.caseflags.request.FlagDetailRequest;
import uk.gov.hmcts.reform.prl.models.caseflags.request.FlagsRequest;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.CitizenSos;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.cafcass.HearingService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.TestUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertNull;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT_WITH_HWF;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseServiceTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "Bearer TestAuthToken";
    public static final String caseId = "1234567891234567";
    public static final String eventId = "1234567891234567";

    public static final String accessCode = "123456";
    private final String eventToken = "eventToken";

    @Mock
    ConfidentialDetailsMapper confidentialDetailsMapper;
    Map<String, Object> applicaionFieldsMap = Map.of(
        "field1", "value1",
        "field2", "value2",
        "field3", "value3"
    );

    @InjectMocks
    private CaseService caseService;
    @Mock
    CaseRepository caseRepository;

    @Mock
    CoreCaseDataApi coreCaseDataApi;


    @Mock
    private CaseData caseDataMock;
    @Mock
    CaseDetailsConverter caseDetailsConverter;

    @Mock
    IdamClient idamClient;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    CcdCoreCaseDataService coreCaseDataService;

    @Mock
    HearingService hearingService;

    @Mock
    SystemUserService systemUserService;

    @Mock
    private CaseSummaryTabService caseSummaryTab;

    @Mock
    CaseDataMapper caseDataMapper;

    @Mock
    CitizenEmailService citizenEmailService;

    @Mock
    AllTabServiceImpl allTabsService;

    @Mock
    CaseEventService caseEventService;

    @Mock
    NoticeOfChangePartiesService noticeOfChangePartiesService;

    @Mock
    CaseUtils caseUtils;

    @Mock
    RoleAssignmentService roleAssignmentService;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    ApplicationsTabService applicationsTabService;

    private CaseData caseData;
    private CaseData caseData2;

    private CaseData caseData3;
    private CaseData caseDataWithOutPartyId;
    private CaseDetails caseDetails;
    private UserDetails userDetails;
    private Map<String, Object> caseDataMap;
    private PartyDetails partyDetails;
    private final String systemUserId = "systemUserID";

    @Mock
    private PartyLevelCaseFlagsService partyLevelCaseFlagsService;
    private CitizenUpdatedCaseData citizenUpdatedCaseData;

    private StartEventResponse startEventResponse;
    private final UUID testUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Before
    public void setup() {
        partyDetails = PartyDetails.builder()
            .firstName("")
            .lastName("test")
            .email("")
            .citizenSosObject(CitizenSos.builder().build())
            .user(User.builder().email("").idamId("").build())
            .build();
        caseData = CaseData.builder()
            .applicants(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"))
                                    .value(partyDetails).build()))
            .respondents(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(YesOrNo.Yes)
                                                                         .partyId(testUuid)
                                                                         .accessCode("123").build()).build()))
            .build();

        caseData2 = CaseData.builder()
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .respondents(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"))
                                     .value(partyDetails).build()))
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(YesOrNo.No)
                                                                         .partyId(testUuid)
                                                                         .accessCode("123").build()).build()))
            .build();

        caseData3 = CaseData.builder()
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .respondents(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .respondentsFL401(partyDetails)
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(No)
                                                                         .accessCode("123").build()).build()))
            .build();


        caseDataMap = new HashMap<>();
        caseDetails = CaseDetails.builder()
            .data(caseDataMap)
            .id(123L)
            .state("SUBMITTED_PAID")
            .build();
        userDetails = UserDetails.builder().id("tesUserId").email("testEmail").build();
        citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .partyDetails(PartyDetails.builder()
                              .firstName("Test")
                              .lastName("User")
                              .user(User.builder()
                                        .email("test@gmail.com")
                                        .idamId("123")
                                        .solicitorRepresented(Yes)
                                        .build())
                              .citizenSosObject(CitizenSos.builder()
                                                    .partiesServed("123,234,1234")
                                                    .build())
                              .build())
            .partyType(PartyEnum.applicant)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(caseRepository.getCase(Mockito.anyString(), Mockito.anyString())).thenReturn(caseDetails);
        when(caseRepository.updateCase(any(), any(), any(), any())).thenReturn(caseDetails);
        when(idamClient.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(coreCaseDataApi.getCase(any(), any(), any())).thenReturn(caseDetails);
        when(coreCaseDataService.startUpdate("", null, "", true)).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        when(coreCaseDataService.startUpdate(null, null, "", true)).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
    }

    @Test
    public void testGetCase() {
        assertNotNull(caseService.getCase("", ""));
    }

    @Test
    public void testRetrieveCases() {
        assertNotNull(caseService.retrieveCases("", ""));
    }

    @Test
    public void testRetrieveCasesTwoParams() {
        assertNotNull(caseService.retrieveCases("", ""));
    }

    @Test
    public void testupdateCaseCitizenUpdate() throws JsonProcessingException {
        CaseDetails caseDetailsAfterUpdate = caseService.updateCase(caseData, "", "","citizen-case-submit");
        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void shouldCreateCase() {
        //Given
        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        when(caseRepository.createCase(authToken, caseData)).thenReturn(caseDetails);

        //When
        CaseDetails actualCaseDetails = caseService.createCase(caseData, authToken);

        //Then
        assertThat(actualCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    public void shouldUpdateCaseForSubmitEvent() throws JsonProcessingException, NotFoundException {
        //Given
        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();
        UserDetails userDetails = UserDetails
            .builder()
            .email("test@gmail.com")
            .build();

        CaseDetails caseDetails = mock(CaseDetails.class);

        CaseData updatedCaseData = caseData.toBuilder()
            .userInfo(wrapElements(UserInfo.builder().emailAddress(userDetails.getEmail()).build()))
            .courtName(PrlAppsConstants.C100_DEFAULT_COURT_NAME)
            .build();

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(caseDataMapper.buildUpdatedCaseData(updatedCaseData)).thenReturn(updatedCaseData);
        when(caseRepository.updateCase(
            authToken,
            caseId,
            updatedCaseData,
            CITIZEN_CASE_SUBMIT
        )).thenReturn(caseDetails);

        //When
        CaseDetails actualCaseDetails =  caseService.updateCase(caseData, authToken, caseId,
                                                                CITIZEN_CASE_SUBMIT.getValue());

        //Then
        assertThat(actualCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    public void shouldUpdateCaseForSubmitEventWithHwf() throws JsonProcessingException, NotFoundException {
        //Given
        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();
        UserDetails userDetails = UserDetails
            .builder()
            .email("test@gmail.com")
            .build();

        CaseDetails caseDetails = mock(CaseDetails.class);

        CaseData updatedCaseData = caseData.toBuilder()
            .userInfo(wrapElements(UserInfo.builder().emailAddress(userDetails.getEmail()).build()))
            .courtName(PrlAppsConstants.C100_DEFAULT_COURT_NAME)
            .build();

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(caseDataMapper.buildUpdatedCaseData(updatedCaseData)).thenReturn(updatedCaseData);
        when(caseRepository.updateCase(authToken, caseId, updatedCaseData, CITIZEN_CASE_SUBMIT_WITH_HWF)).thenReturn(
            caseDetails);

        //When
        CaseDetails actualCaseDetails =  caseService.updateCase(caseData, authToken, caseId,
                                                                CITIZEN_CASE_SUBMIT_WITH_HWF.getValue());

        //Then
        assertThat(actualCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    public void testGetPartyCaseFlags() {
        User user1 = User.builder().idamId("applicant-1").build();
        User user2 = User.builder().idamId("respondent-1").build();
        User user3 = User.builder().idamId("respondent-2").build();
        PartyDetails applicant = PartyDetails.builder().user(user1).email("testappl@hmcts.net").firstName(
            "Applicant 1 FN").lastName("Applicant 1 LN").build();
        PartyDetails respondent1 = PartyDetails.builder().user(user2).email("testresp1@hmcts.net").firstName(
            "Respondent 1 FN").lastName("Respondent 1 LN").build();
        PartyDetails respondent2 = PartyDetails.builder().user(user3).email("testresp2@hmcts.net").firstName(
            "Respondent 2 FN").lastName("Respondent 2 LN").build();

        FlagDetail flagDetailRequestForFillingForms = FlagDetail.builder()
            .name("Support filling in forms")
            .name_cy("Cymorth i lenwi ffurflenni")
            .hearingRelevant(No)
            .flagCode("RA0018")
            .status("Requested")
            .availableExternally(Yes)
            .build();
        FlagDetail flagDetailRequestForHearing = FlagDetail.builder()
            .name("Private waiting area")
            .name_cy("Ystafell aros breifat")
            .hearingRelevant(Yes)
            .flagCode("RA0033")
            .status("Requested")
            .availableExternally(Yes)
            .build();

        Flags applicant1PartyFlags = Flags.builder().roleOnCase("Applicant 1").partyName("Applicant 1 FN Applicant 1 LN").details(
            Collections.singletonList(element(flagDetailRequestForFillingForms))).build();
        Flags respondent1PartyFlags = Flags.builder().roleOnCase("Respondent 1").partyName(
            "Respondent 1 FN Respondent 1 LN").details(Collections.singletonList(element(flagDetailRequestForHearing))).build();
        Flags respondent2PartyFlags = Flags.builder().roleOnCase("Respondent 2").partyName(
            "Respondent 2 FN Respondent 2 LN").details(Arrays.asList(
            element(flagDetailRequestForFillingForms),
            element(flagDetailRequestForHearing)
        )).build();

        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .caseTypeOfApplication("C100")
            .applicants(Collections.singletonList(element(applicant)))
            .respondents(Arrays.asList(element(respondent1), element(respondent2)))
            .allPartyFlags(AllPartyFlags.builder().caApplicant1ExternalFlags(applicant1PartyFlags).caRespondent1ExternalFlags(
                respondent1PartyFlags).caRespondent2ExternalFlags(respondent2PartyFlags).build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1234567891234567L)
            .data(stringObjectMap)
            .build();
        when(caseService.getCase(authToken, caseId)).thenReturn(caseDetails);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        // Unhappy path - when the request is valid, but party details is invalid.
        Flags invalidUserExternalFlag = caseService.getPartyCaseFlags(authToken, caseId, "applicant-2");
        Assert.assertNull(invalidUserExternalFlag);

        // Happy path 1 - when the request is valid and respondent party external flags is retrieved from the existing case data.
        when(partyLevelCaseFlagsService.getPartyCaseDataExternalField(
            "C100",
            PartyRole.Representing.CARESPONDENT,
            1
        )).thenReturn("caRespondent2ExternalFlags");
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Flags.class))).thenReturn(respondent2PartyFlags);
        Flags respondentExternalFlag = caseService.getPartyCaseFlags(authToken, caseId, "respondent-2");
        Assert.assertNotNull(respondentExternalFlag);
        Assert.assertEquals("Respondent 2 FN Respondent 2 LN", respondentExternalFlag.getPartyName());

        // Happy path 2 - when the request is valid and applicant party external flags is retrieved from the existing case data.
        when(partyLevelCaseFlagsService.getPartyCaseDataExternalField(
            "C100",
            PartyRole.Representing.CAAPPLICANT,
            0
        )).thenReturn("caApplicant1ExternalFlags");
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Flags.class))).thenReturn(applicant1PartyFlags);
        Flags applicantExternalFlag = caseService.getPartyCaseFlags(authToken, caseId, "applicant-1");
        Assert.assertNotNull(applicantExternalFlag);
        Assert.assertEquals(applicant1PartyFlags, applicantExternalFlag);
    }

    @Test
    public void testUpdateCitizenRaFlags() {
        User user1 = User.builder().idamId("applicant-1").build();
        User user2 = User.builder().idamId("respondent-1").build();
        User user3 = User.builder().idamId("respondent-2").build();
        PartyDetails applicant = PartyDetails.builder().user(user1).email("testappl@hmcts.net").firstName(
            "Applicant 1 FN").lastName("Applicant 1 LN").build();
        PartyDetails respondent1 = PartyDetails.builder().user(user2).email("testresp1@hmcts.net").firstName(
            "Respondent 1 FN").lastName("Respondent 1 LN").build();
        PartyDetails respondent2 = PartyDetails.builder().user(user3).email("testresp2@hmcts.net").firstName(
            "Respondent 2 FN").lastName("Respondent 2 LN").build();

        FlagDetail flagDetailRequestForFillingForms = FlagDetail.builder()
            .name("Support filling in forms")
            .name_cy("Cymorth i lenwi ffurflenni")
            .hearingRelevant(No)
            .flagCode("RA0018")
            .status("Requested")
            .dateTimeCreated(LocalDateTime.parse(
                "2023-11-11T12:12:12.000Z",
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            ))
            .availableExternally(Yes)
            .build();
        FlagDetail flagDetailRequestForHearing = FlagDetail.builder()
            .name("Private waiting area")
            .name_cy("Ystafell aros breifat")
            .hearingRelevant(Yes)
            .flagCode("RA0033")
            .status("Requested")
            .dateTimeCreated(LocalDateTime.parse(
                "2023-11-11T12:13:02.000Z",
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            ))
            .availableExternally(Yes)
            .build();

        Element<FlagDetailRequest>  updateFlagDetailRequestForFillingForms = element(FlagDetailRequest.builder()
            .name("Support filling in forms")
            .name_cy("Cymorth i lenwi ffurflenni")
            .hearingRelevant(YesOrNo.No)
            .flagCode("RA0018")
            .status("Inactive")
            .dateTimeCreated(LocalDateTime.parse(
                "2023-11-11T12:13:02.000Z",
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            ))
            .dateTimeModified(LocalDateTime.parse(
                "2023-11-12T10:09:21.000Z",
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            ))
            .availableExternally(YesOrNo.Yes)
            .build());

        Element<FlagDetailRequest>  updateFlagDetailRequestForHearing = element(FlagDetailRequest.builder()
            .name("Private waiting area")
            .name_cy("Ystafell aros breifat")
            .hearingRelevant(Yes)
            .flagCode("RA0033")
            .status("Inactive")
            .dateTimeCreated(LocalDateTime.parse(
                "2023-11-11T12:13:02.000Z",
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            ))
            .dateTimeModified(LocalDateTime.parse(
                "2023-11-12T10:09:21.000Z",
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            ))
            .availableExternally(Yes)
            .build());

        Flags applicant1PartyFlags = Flags.builder().roleOnCase("Applicant 1").partyName("Applicant 1 FN Applicant 1 LN").details(
            Collections.singletonList(element(flagDetailRequestForFillingForms))).build();
        Flags respondent1PartyFlags = Flags.builder().roleOnCase("Respondent 1").partyName(
            "Respondent 1 FN Respondent 1 LN").details(Collections.singletonList(element(flagDetailRequestForHearing))).build();
        Flags respondent2PartyFlags = Flags.builder().roleOnCase("Respondent 2").partyName(
            "Respondent 2 FN Respondent 2 LN").details(Arrays.asList(
            element(flagDetailRequestForFillingForms),
            element(flagDetailRequestForHearing)
        )).build();
        CitizenPartyFlagsRequest updatePartyFlagsRequest;
        updatePartyFlagsRequest = CitizenPartyFlagsRequest.builder()
            .caseTypeOfApplication("C100")
            .partyIdamId("respondent-1")
            .partyExternalFlags(FlagsRequest.builder()
                                    .details(Arrays.asList(
                                        updateFlagDetailRequestForFillingForms,
                                        updateFlagDetailRequestForHearing
                                    )).build()).build();

        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .caseTypeOfApplication("C100")
            .applicants(Collections.singletonList(element(applicant)))
            .respondents(Arrays.asList(element(respondent1), element(respondent2)))
            .allPartyFlags(AllPartyFlags.builder().caApplicant1ExternalFlags(applicant1PartyFlags).caRespondent1ExternalFlags(
                respondent1PartyFlags).caRespondent2ExternalFlags(respondent2PartyFlags).build())
            .build();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Map<String, Object> stringObjectMap = caseData.toMap(mapper);
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1234567891234567L)
            .data(stringObjectMap)
            .build();
        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);

        when(coreCaseDataService.eventRequest(CaseEvent.fromValue("c100RequestSupport"), systemUserId)).thenReturn(
            EventRequestData.builder().build());
        StartEventResponse startEventResponse = StartEventResponse.builder().eventId("c100RequestSupport")
            .caseDetails(caseDetails)
            .token(eventToken).build();
        // when(coreCaseDataService.startUpdate(
        //    authToken,
        //    EventRequestData.builder().build(),
        //    caseId,
        //    false
        //)).thenReturn(
        //    startEventResponse);
        when(coreCaseDataService.startUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(
            startEventResponse);
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(CaseData.class))).thenReturn(caseData);

        // Happy path 1 - when the request is valid and respondent party external flags is updated in the response.
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Flags.class))).thenReturn(respondent1PartyFlags);
        when(partyLevelCaseFlagsService.getPartyCaseDataExternalField(
            "C100",
            PartyRole.Representing.CARESPONDENT,
            0
        )).thenReturn("caRespondent1ExternalFlags");
        ResponseEntity<Object> updatePartyFlagsResponse;
        updatePartyFlagsResponse = caseService.updateCitizenRAflags(
            caseId,
            "c100RequestSupport",
            authToken,
            updatePartyFlagsRequest
        );
        assertThat(updatePartyFlagsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Happy path 2 - when the request is valid and applicant party external flags is updated in the response.
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Flags.class))).thenReturn(applicant1PartyFlags);
        when(partyLevelCaseFlagsService.getPartyCaseDataExternalField(
            "C100",
            PartyRole.Representing.CAAPPLICANT,
            0
        )).thenReturn("caApplicant1ExternalFlags");
        updatePartyFlagsRequest = CitizenPartyFlagsRequest.builder()
            .caseTypeOfApplication("C100")
            .partyIdamId("applicant-1")
            .partyExternalFlags(FlagsRequest.builder()
                                    .details(Arrays.asList(
                                        updateFlagDetailRequestForFillingForms,
                                        updateFlagDetailRequestForHearing
                                    )).build()).build();
        updatePartyFlagsResponse = caseService.updateCitizenRAflags(
            caseId,
            "c100RequestSupport",
            authToken,
            updatePartyFlagsRequest
        );
        assertThat(updatePartyFlagsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Unhappy path 1 - when the request is not valid.
        updatePartyFlagsRequest = CitizenPartyFlagsRequest.builder()
            .caseTypeOfApplication("C100")
            .partyIdamId("respondent-1").build();
        updatePartyFlagsResponse = caseService.updateCitizenRAflags(
            caseId,
            "c100RequestSupport",
            authToken,
            updatePartyFlagsRequest
        );
        assertThat(updatePartyFlagsResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Unhappy path 2 - when the request is valid, but party details is invalid.
        updatePartyFlagsRequest = CitizenPartyFlagsRequest.builder()
            .caseTypeOfApplication("C100")
            .partyIdamId("respondent-3").partyExternalFlags(FlagsRequest.builder().build()).build();
        updatePartyFlagsResponse = caseService.updateCitizenRAflags(
            caseId,
            "c100RequestSupport",
            authToken,
            updatePartyFlagsRequest
        );
        assertThat(updatePartyFlagsResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(updatePartyFlagsResponse.getBody()).isEqualTo("party details not found");

        // Unhappy path 3 - when the request is valid, but party external flag details not present in the case data.
        when(partyLevelCaseFlagsService.getPartyCaseDataExternalField(
            "C100",
            PartyRole.Representing.CARESPONDENT,
            6
        )).thenReturn("caRespondent7ExternalFlags");
        updatePartyFlagsRequest = CitizenPartyFlagsRequest.builder()
            .caseTypeOfApplication("C100")
            .partyIdamId("respondent-2").partyExternalFlags(FlagsRequest.builder().build()).build();
        updatePartyFlagsResponse = caseService.updateCitizenRAflags(
            caseId,
            "c100RequestSupport",
            authToken,
            updatePartyFlagsRequest
        );
        assertThat(updatePartyFlagsResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(updatePartyFlagsResponse.getBody()).isEqualTo("party external flag details not found");
    }

    @Test
    public void shouldUpdateCaseWithCaseName() throws IOException, NotFoundException {

        C100RebuildData c100RebuildData = C100RebuildData.builder()
            .c100RebuildApplicantDetails(TestUtil.readFileFrom("classpath:c100-rebuild/appl.json"))
            .c100RebuildRespondentDetails(TestUtil.readFileFrom("classpath:c100-rebuild/resp.json"))
            .build();

        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .c100RebuildData(c100RebuildData)
            .build();
        UserDetails userDetails = UserDetails
            .builder()
            .email("test@gmail.com")
            .build();

        CaseDetails caseDetails = mock(CaseDetails.class);

        CaseData updatedCaseData = caseData.toBuilder()
            .id(1234567891234567L)
            .c100RebuildData(c100RebuildData)
            .applicantCaseName("applicantLN1 V respLN1")
            .build();

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(caseDataMapper.buildUpdatedCaseData(updatedCaseData)).thenReturn(updatedCaseData);
        when(caseRepository.updateCase(authToken, caseId, updatedCaseData, CITIZEN_CASE_UPDATE)).thenReturn(caseDetails);

        //When
        CaseDetails actualCaseDetails =  caseService.updateCase(caseData, authToken, caseId,
            CITIZEN_CASE_UPDATE.getValue());

        //Then
        assertThat(actualCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    public void shouldUpdateCaseWithCaseNameButNoApplicantOrRespondentDetails() throws IOException, NotFoundException {

        C100RebuildData c100RebuildData = C100RebuildData.builder()
            .build();

        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .c100RebuildData(c100RebuildData)
            .build();
        UserDetails userDetails = UserDetails
            .builder()
            .email("test@gmail.com")
            .build();

        CaseDetails caseDetails = mock(CaseDetails.class);

        CaseData updatedCaseData = caseData.toBuilder()
            .id(1234567891234567L)
            .c100RebuildData(c100RebuildData)
            .build();

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(caseDataMapper.buildUpdatedCaseData(updatedCaseData)).thenReturn(updatedCaseData);
        when(caseRepository.updateCase(authToken, caseId, updatedCaseData, CITIZEN_CASE_UPDATE)).thenReturn(caseDetails);

        //When
        CaseDetails actualCaseDetails =  caseService.updateCase(caseData, authToken, caseId,
            CITIZEN_CASE_UPDATE.getValue());

        //Then
        assertThat(actualCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    public void shouldUpdateCaseWithCaseNameButNoC100RebuildData() throws IOException, NotFoundException {

        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .build();
        UserDetails userDetails = UserDetails
            .builder()
            .email("test@gmail.com")
            .build();

        CaseDetails caseDetails = mock(CaseDetails.class);

        CaseData updatedCaseData = caseData.toBuilder()
            .id(1234567891234567L)
            .build();

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(caseDataMapper.buildUpdatedCaseData(updatedCaseData)).thenReturn(updatedCaseData);
        when(caseRepository.updateCase(authToken, caseId, updatedCaseData, CITIZEN_CASE_UPDATE)).thenReturn(caseDetails);

        //When
        CaseDetails actualCaseDetails =  caseService.updateCase(caseData, authToken, caseId,
            CITIZEN_CASE_UPDATE.getValue());

        //Then
        assertThat(actualCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    public void shouldUpdateCaseWithCaseNameButCaseNameExists() throws IOException, NotFoundException {

        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();
        UserDetails userDetails = UserDetails
            .builder()
            .email("test@gmail.com")
            .build();

        CaseDetails caseDetails = mock(CaseDetails.class);

        CaseData updatedCaseData = caseData.toBuilder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(caseDataMapper.buildUpdatedCaseData(updatedCaseData)).thenReturn(updatedCaseData);
        when(caseRepository.updateCase(authToken, caseId, updatedCaseData, CITIZEN_CASE_UPDATE)).thenReturn(caseDetails);

        //When
        CaseDetails actualCaseDetails =  caseService.updateCase(caseData, authToken, caseId,
            CITIZEN_CASE_UPDATE.getValue());

        //Then
        assertThat(actualCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    public void getCaseWithHearing() {
        when(coreCaseDataService.findCaseById(authToken, caseId)).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        when(hearingService.getHearings(authToken, caseId)).thenReturn(Hearings.hearingsWith().build());
        CaseDataWithHearingResponse caseDataWithHearingResponse = caseService.getCaseWithHearing(authToken, caseId, "yes");
        assertNotNull(caseDataWithHearingResponse.getHearings());
    }

    @Test
    public void getCaseWithHearingHearingNotNeeded() {
        when(coreCaseDataService.findCaseById(authToken, caseId)).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        when(hearingService.getHearings(authToken, caseId)).thenReturn(Hearings.hearingsWith().build());
        CaseDataWithHearingResponse caseDataWithHearingResponse = caseService.getCaseWithHearing(authToken, caseId, "dud");
        assertNull(caseDataWithHearingResponse.getHearings());
    }
}
