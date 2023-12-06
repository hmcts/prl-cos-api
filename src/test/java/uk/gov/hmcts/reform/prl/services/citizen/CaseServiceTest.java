package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javassist.NotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.UpdateCaseData;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.caseflags.request.CitizenPartyFlagsRequest;
import uk.gov.hmcts.reform.prl.models.caseflags.request.FlagDetailRequest;
import uk.gov.hmcts.reform.prl.models.caseflags.request.FlagsRequest;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT_WITH_HWF;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_WITHDRAW;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseServiceTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "Bearer TestAuthToken";
    public static final String caseId = "1234567891234567";
    public static final String accessCode = "123456";
    public static final String INVALID = "Invalid";
    private static final String TEST_UUID = "00000000-0000-0000-0000-000000000000";

    @InjectMocks
    private CaseService caseService;
    @Mock
    CaseRepository caseRepository;

    @Mock
    CoreCaseDataApi coreCaseDataApi;

    @Mock
    CaseDetailsConverter caseDetailsConverter;

    @Mock
    IdamClient idamClient;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    CcdCoreCaseDataService coreCaseDataService;

    @Mock
    SystemUserService systemUserService;

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

    private CaseData caseData;
    private CaseData caseData2;

    private CaseData caseData3;
    private CaseData caseDataWithOutPartyId;
    private CaseDetails caseDetails;
    private UserDetails userDetails;
    private Map<String, Object> caseDataMap;
    private PartyDetails partyDetails;
    private UpdateCaseData updateCaseData;
    private final String systemUserId = "systemUserID";
    private final String eventToken = "eventToken";

    @Mock
    private PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    @Before
    public void setup() {
        partyDetails = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .build();
        caseData = CaseData.builder()
            .applicants(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"))
                                    .value(partyDetails).build()))
            .respondents(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(Yes)
                                                                         .partyId(UUID.fromString(
                                                                             "00000000-0000-0000-0000-000000000000"))
                                                                         .accessCode("123").build()).build()))
            .build();

        caseData2 = CaseData.builder()
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .respondents(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"))
                                     .value(partyDetails).build()))
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(No)
                                                                         .partyId(UUID.fromString(
                                                                             "00000000-0000-0000-0000-000000000000"))
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
        updateCaseData = UpdateCaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .partyDetails(PartyDetails.builder()
                              .firstName("Test")
                              .lastName("User")
                              .user(User.builder()
                                        .email("test@gmail.com")
                                        .idamId("123")
                                        .solicitorRepresented(Yes)
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
    public void testupdateCaseRespondent() throws JsonProcessingException {
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData2);
        CaseDetails caseDetailsAfterUpdate = caseService.updateCase(caseData2, "", "", "", "linkCase", "123");
        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testupdateCaseRespondentNoPartyId() throws JsonProcessingException {
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData3);
        CaseDetails caseDetailsAfterUpdate = caseService.updateCase(caseData2, "", "", "", "linkCase", "123");
        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testupdateCaseApplicant() throws JsonProcessingException {
        CaseDetails caseDetailsAfterUpdate = caseService.updateCase(caseData, "", "", "", "linkCase", "123");
        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testupdateCaseOfApplicantWithOutPartyId() throws JsonProcessingException {
        User user = User.builder().build();
        PartyDetails partyDetailsWithUser = PartyDetails.builder().user(user)
            .firstName("")
            .lastName("")
            .build();
        caseDataWithOutPartyId = CaseData.builder()
            .applicantsFL401(partyDetailsWithUser)
            .respondents(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(Yes)
                                                                         .partyId(null)
                                                                         .accessCode("1234").build()).build()))
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseDataWithOutPartyId);
        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);

        CaseDetails caseDetailsAfterUpdate = caseService.updateCase(
            caseDataWithOutPartyId,
            "",
            "",
            "",
            "linkCase",
            "1234"
        );
        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testupdateCaseOfRespondentWithOutPartyId() throws JsonProcessingException {
        User user = User.builder().build();
        PartyDetails partyDetailsWithUser = PartyDetails.builder().user(user)
            .firstName("")
            .lastName("")
            .build();
        caseDataWithOutPartyId = CaseData.builder()
            .respondentsFL401(partyDetailsWithUser)
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(No)
                                                                         .partyId(null)
                                                                         .accessCode("1234").hasLinked("Yes").build()).build()))
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseDataWithOutPartyId);
        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);

        CaseDetails caseDetailsAfterUpdate = caseService.updateCase(
            caseDataWithOutPartyId,
            "",
            "",
            "",
            "linkCase",
            "1234"
        );
        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testGetCase() {
        assertNotNull(caseService.getCase("", ""));
    }

    @Test
    public void testValidateAccessCode() {
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(null);
        assertNotNull(caseService.validateAccessCode("", "", "", ""));
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
        CaseDetails caseDetailsAfterUpdate = caseService.updateCase(caseData, "", "", "", "citizen-case-submit", "123");
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
        CaseDetails actualCaseDetails = caseService.updateCase(caseData, authToken, s2sToken, caseId,
                                                               CITIZEN_CASE_SUBMIT.getValue(), accessCode
        );

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
        CaseDetails actualCaseDetails = caseService.updateCase(caseData, authToken, s2sToken, caseId,
                                                               CITIZEN_CASE_SUBMIT_WITH_HWF.getValue(), accessCode
        );

        //Then
        assertThat(actualCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    public void shouldWithdrawCase() {
        //Given
        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .withDrawApplicationData(
                WithdrawApplication.builder()
                    .withDrawApplication(Yes)
                    .withDrawApplicationReason("Case withdrawn").build())
            .build();
        UserDetails userDetails = UserDetails
            .builder()
            .email("test@gmail.com")
            .build();

        CaseDetails caseDetails = mock(CaseDetails.class);

        CaseData updatedCaseData = caseData.toBuilder()
            .userInfo(wrapElements(UserInfo.builder().emailAddress(userDetails.getEmail()).build()))
            .courtName(PrlAppsConstants.C100_DEFAULT_COURT_NAME)
            .state(State.CASE_WITHDRAWN)
            .build();

        List<CaseEventDetail> caseEventDetails = Arrays.asList(
            CaseEventDetail.builder().stateId(PrlAppsConstants.SUBMITTED_STATE).build());

        when(caseEventService.findEventsForCase(caseId)).thenReturn(caseEventDetails);
        when(caseService.getCase(authToken, caseId)).thenReturn(caseDetails);
        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        //when(caseDataMapper.buildUpdatedCaseData(updatedCaseData)).thenReturn(updatedCaseData);
        when(caseRepository.updateCase(authToken, caseId, updatedCaseData, CITIZEN_CASE_WITHDRAW)).thenReturn(
            caseDetails);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        //When
        CaseDetails actualCaseDetails = caseService.withdrawCase(caseData, caseId, authToken);

        //Then
        assertNotNull(actualCaseDetails);
    }


    @Test
    public void testUpdateCaseDetailsCitizenUpdateOnCaApplicant() throws JsonProcessingException {

        User user1 = User.builder().idamId("123").build();
        PartyDetails applicant1 = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();
        PartyDetails applicant2 = PartyDetails.builder().email("test@hmcts.net").firstName("test").build();
        caseData = CaseData.builder()
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .build();
        caseDataMap = caseData.toMap(objectMapper);

        caseDetails = caseDetails.toBuilder()
            .data(caseDataMap)
            .id(123L)
            .state("SUBMITTED_PAID")
            .build();
        PartyDetails partyDetails1 = PartyDetails.builder()
            .firstName("Test")
            .lastName("User")
            .user(User.builder()
                      .email("test@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(Yes)
                      .build())
            .build();
        updateCaseData = UpdateCaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.applicant)
            .build();


        userDetails = UserDetails.builder().build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(caseRepository.getCase(authToken, "123")).thenReturn(caseDetails);
        when(caseRepository.updateCase(authToken, "123", caseData, CITIZEN_CASE_UPDATE)).thenReturn(caseDetails);

        CaseDetails caseDetailsAfterUpdate = caseService.updateCaseDetails(
            authToken,
            "123",
            "citizen-case-submit",
            updateCaseData
        );

        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testUpdateCaseDetailsCitizenUpdateOnCaRespondent() throws JsonProcessingException {

        User user1 = User.builder().idamId("123").build();
        PartyDetails respondent1 = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();
        PartyDetails respondent2 = PartyDetails.builder().email("test@hmcts.net").firstName("test").build();
        caseData = CaseData.builder()
            .respondents(Arrays.asList(element(respondent1), element(respondent2)))
            .build();
        caseDataMap = caseData.toMap(objectMapper);

        caseDetails = caseDetails.toBuilder()
            .data(caseDataMap)
            .id(123L)
            .state("SUBMITTED_PAID")
            .build();
        PartyDetails partyDetails1 = PartyDetails.builder()
            .firstName("Test")
            .lastName("User")
            .user(User.builder()
                      .email("test@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(Yes)
                      .build())
            .build();
        updateCaseData = UpdateCaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.respondent)
            .build();


        userDetails = UserDetails.builder().build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(caseRepository.getCase(authToken, "123")).thenReturn(caseDetails);
        when(caseRepository.updateCase(authToken, "123", caseData, CITIZEN_CASE_UPDATE)).thenReturn(caseDetails);

        CaseDetails caseDetailsAfterUpdate = caseService.updateCaseDetails(
            authToken,
            "123",
            "citizen-case-submit",
            updateCaseData
        );

        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testUpdateCaseDetailsCitizenUpdateOnCaRespondentForNull() throws JsonProcessingException {

        User user1 = User.builder().idamId("123").build();
        PartyDetails respondent1 = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();
        PartyDetails respondent2 = PartyDetails.builder().email("test@hmcts.net").firstName("test").build();
        caseData = CaseData.builder()
            .applicants(Arrays.asList(element(respondent1), element(respondent2)))
            .build();
        caseDataMap = caseData.toMap(objectMapper);

        caseDetails = caseDetails.toBuilder()
            .data(caseDataMap)
            .id(123L)
            .state("SUBMITTED_PAID")
            .build();
        PartyDetails partyDetails1 = PartyDetails.builder()
            .firstName("Test")
            .lastName("User")
            .build();
        updateCaseData = UpdateCaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.applicant)
            .build();


        userDetails = UserDetails.builder().build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(caseRepository.getCase(authToken, "123")).thenReturn(caseDetails);
        when(caseRepository.updateCase(authToken, "123", caseData, CITIZEN_CASE_UPDATE)).thenReturn(caseDetails);

        Assert.assertThrows(
            RuntimeException.class,
            () -> caseService.updateCaseDetails(authToken, "123", "citizen-case-submit", updateCaseData)
        );
    }


    @Test
    public void testUpdateCaseDetailsCitizenUpdateOnDaRespondent() throws JsonProcessingException {
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("Test")
            .lastName("User")
            .user(User.builder()
                      .email("testparty@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(Yes)
                      .build())
            .build();
        caseData = CaseData.builder()
            .respondentsFL401(partyDetails)
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(Yes)
                                                                         .partyId(UUID.fromString(
                                                                             "00000000-0000-0000-0000-000000000000"))
                                                                         .accessCode("123").build()).build()))
            .build();
        caseDataMap = new HashMap<>();
        caseDetails = CaseDetails.builder()
            .data(caseDataMap)
            .id(123L)
            .state("SUBMITTED_PAID")
            .build();

        PartyDetails partyDetails1 = PartyDetails.builder()
            .firstName("Test")
            .lastName("User")
            .user(User.builder()
                      .email("test@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(Yes)
                      .build())
            .build();
        updateCaseData = UpdateCaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.respondent)
            .build();
        userDetails = UserDetails.builder().build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(caseRepository.getCase(Mockito.anyString(), Mockito.anyString())).thenReturn(caseDetails);
        when(caseRepository.updateCase(any(), any(), any(), any())).thenReturn(caseDetails);
        when(idamClient.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(coreCaseDataApi.getCase(any(), any(), any())).thenReturn(caseDetails);
        when(coreCaseDataService.startUpdate("", null, "", true)).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        when(coreCaseDataService.startUpdate(null, null, "", true)).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        CaseDetails caseDetailsAfterUpdate = caseService.updateCaseDetails(
            authToken,
            "123",
            "citizen-case-submit",
            updateCaseData
        );
        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testUpdateCaseDetailsCitizenUpdateOnDaApplicant() throws JsonProcessingException {
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("Test")
            .lastName("User")
            .user(User.builder()
                      .email("testparty@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(Yes)
                      .build())
            .build();
        caseData = CaseData.builder()
            .applicantsFL401(partyDetails)
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(Yes)
                                                                         .partyId(UUID.fromString(
                                                                             "00000000-0000-0000-0000-000000000000"))
                                                                         .accessCode("123").build()).build()))
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(objectMapper);
        caseDetails = CaseDetails.builder()
            .data(caseDataMap)
            .id(123L)
            .state("SUBMITTED_PAID")
            .build();

        PartyDetails partyDetails1 = PartyDetails.builder()
            .firstName("Test")
            .lastName("User")
            .user(User.builder()
                      .email("test@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(Yes)
                      .build())
            .build();
        updateCaseData = UpdateCaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.applicant)
            .build();
        userDetails = UserDetails.builder().build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(caseRepository.getCase(Mockito.anyString(), Mockito.anyString())).thenReturn(caseDetails);
        when(caseRepository.updateCase(any(), any(), any(), any())).thenReturn(caseDetails);
        when(idamClient.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(coreCaseDataApi.getCase(any(), any(), any())).thenReturn(caseDetails);
        when(coreCaseDataService.startUpdate("", null, "", true)).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        when(coreCaseDataService.startUpdate(null, null, "", true)).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        CaseDetails caseDetailsAfterUpdate = caseService.updateCaseDetails(
            authToken,
            "123",
            "citizen-case-submit",
            updateCaseData
        );
        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testValidateAccessCodeForInvalidCase() {

        when(caseService.getCase(authToken, caseId)).thenReturn(null);

        String isValid = caseService.validateAccessCode(authToken, s2sToken, caseId, accessCode);

        assertEquals(INVALID, isValid);
    }

    @Test
    public void testValidateAccessCodeForEmptyCaseInvites() {
        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .caseInvites(null).build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1234567891234567L)
            .data(stringObjectMap)
            .build();
        when(coreCaseDataApi.getCase(authToken, s2sToken, caseId)).thenReturn(caseDetails);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        String isValid = caseService.validateAccessCode(authToken, s2sToken, caseId, accessCode);

        assertEquals(INVALID, isValid);
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
    @Ignore
    public void testUpdateCitizenRaflags() {
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

        FlagDetailRequest updateFlagDetailRequestForFillingForms = FlagDetailRequest.builder()
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
            .build();

        FlagDetailRequest updateFlagDetailRequestForHearing = FlagDetailRequest.builder()
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
        when(coreCaseDataService.startUpdate(
            authToken,
            EventRequestData.builder().build(),
            caseId,
            false
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
        updatePartyFlagsResponse = caseService.createCitizenReasonableAdjustmentsFlags(
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
        updatePartyFlagsResponse = caseService.createCitizenReasonableAdjustmentsFlags(
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
        updatePartyFlagsResponse = caseService.createCitizenReasonableAdjustmentsFlags(
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
        updatePartyFlagsResponse = caseService.createCitizenReasonableAdjustmentsFlags(
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
        updatePartyFlagsResponse = caseService.createCitizenReasonableAdjustmentsFlags(
            caseId,
            "c100RequestSupport",
            authToken,
            updatePartyFlagsRequest
        );
        assertThat(updatePartyFlagsResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(updatePartyFlagsResponse.getBody()).isEqualTo("party external flag details not found");
    }
}
