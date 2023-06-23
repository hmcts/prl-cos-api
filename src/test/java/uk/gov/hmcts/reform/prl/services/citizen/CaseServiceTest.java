package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.UpdateCaseData;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT_WITH_HWF;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_WITHDRAW;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseServiceTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "Bearer TestAuthToken";
    public static final String caseId = "1234567891234567";
    public static final String accessCode = "123456";
    public static final String INVALID = "Invalid";

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
    private CaseData caseDataWithOutPartyId;
    private CaseDetails caseDetails;
    private UserDetails userDetails;
    private Map<String, Object> caseDataMap;
    private PartyDetails partyDetails;
    private UpdateCaseData updateCaseData;

    @Before
    public void setup() {
        partyDetails = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .build();
        caseData = CaseData.builder()
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .respondents(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(YesOrNo.Yes)
                                                                         .partyId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
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
                                        .solicitorRepresented(YesOrNo.Yes)
                                        .build())
                              .build())
            .partyType(PartyEnum.applicant)
            .build();
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseData);
        when(caseRepository.getCase(Mockito.anyString(), Mockito.anyString())).thenReturn(caseDetails);
        when(caseRepository.updateCase(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(caseDetails);
        when(idamClient.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(coreCaseDataApi.getCase(Mockito.any(),Mockito.any(), Mockito.any())).thenReturn(caseDetails);
        when(coreCaseDataService.startUpdate("", null, "", true)).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        when(coreCaseDataService.startUpdate(null, null, "", true)).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
    }

    @Test
    public void testupdateCase() throws JsonProcessingException {
        CaseDetails caseDetailsAfterUpdate = caseService.updateCase(caseData, "", "","","linkCase","123");
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
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(YesOrNo.Yes)
                                                                         .partyId(null)
                                                                         .accessCode("1234").build()).build()))
            .build();
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseDataWithOutPartyId);
        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);

        CaseDetails caseDetailsAfterUpdate = caseService.updateCase(caseDataWithOutPartyId, "", "","","linkCase","1234");
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
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(YesOrNo.No)
                                                                         .partyId(null)
                                                                         .accessCode("1234").build()).build()))
            .build();
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseDataWithOutPartyId);
        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);

        CaseDetails caseDetailsAfterUpdate = caseService.updateCase(caseDataWithOutPartyId, "", "","","linkCase","1234");
        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testGetCase() {
        assertNotNull(caseService.getCase("",""));
    }

    @Test
    public void testValidateAccessCode() {
        assertNotNull(caseService.validateAccessCode("","","",""));
    }

    @Test
    public void testRetrieveCases() {
        assertNotNull(caseService.retrieveCases("",""));
    }

    @Test
    public void testRetrieveCasesTwoParams() {
        assertNotNull(caseService.retrieveCases("",""));
    }

    @Test
    public void testupdateCaseCitizenUpdate() throws JsonProcessingException {
        CaseDetails caseDetailsAfterUpdate = caseService.updateCase(caseData, "", "","","citizen-case-submit","123");
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
        CaseDetails actualCaseDetails =  caseService.createCase(caseData, authToken);

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
        when(caseRepository.updateCase(authToken, caseId, updatedCaseData, CITIZEN_CASE_SUBMIT)).thenReturn(caseDetails);

        //When
        CaseDetails actualCaseDetails =  caseService.updateCase(caseData, authToken, s2sToken, caseId,
                                                                CITIZEN_CASE_SUBMIT.getValue(), accessCode);

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
        when(caseRepository.updateCase(authToken, caseId, updatedCaseData, CITIZEN_CASE_SUBMIT_WITH_HWF)).thenReturn(caseDetails);

        //When
        CaseDetails actualCaseDetails =  caseService.updateCase(caseData, authToken, s2sToken, caseId,
                                                                CITIZEN_CASE_SUBMIT_WITH_HWF.getValue(), accessCode);

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
                    .withDrawApplication(YesOrNo.Yes)
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
        when(caseRepository.updateCase(authToken, caseId, updatedCaseData, CITIZEN_CASE_WITHDRAW)).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseData);

        //When
        CaseDetails actualCaseDetails =  caseService.withdrawCase(caseData, caseId, authToken);

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
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .build();
        updateCaseData = UpdateCaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.applicant)
            .build();


        userDetails = UserDetails.builder().build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(caseRepository.getCase(authToken,"123")).thenReturn(caseDetails);
        when(caseRepository.updateCase(authToken, "123", caseData, CITIZEN_CASE_UPDATE)).thenReturn(caseDetails);

        CaseDetails caseDetailsAfterUpdate = caseService.updateCaseDetails(authToken, "123", "citizen-case-submit", updateCaseData);

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
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .build();
        updateCaseData = UpdateCaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.respondent)
            .build();


        userDetails = UserDetails.builder().build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(caseRepository.getCase(authToken,"123")).thenReturn(caseDetails);
        when(caseRepository.updateCase(authToken, "123", caseData, CITIZEN_CASE_UPDATE)).thenReturn(caseDetails);

        CaseDetails caseDetailsAfterUpdate = caseService.updateCaseDetails(authToken, "123", "citizen-case-submit", updateCaseData);

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
        when(caseRepository.getCase(authToken,"123")).thenReturn(caseDetails);
        when(caseRepository.updateCase(authToken, "123", caseData, CITIZEN_CASE_UPDATE)).thenReturn(caseDetails);

        Assert.assertThrows(RuntimeException.class, () -> caseService.updateCaseDetails(authToken, "123", "citizen-case-submit", updateCaseData));
    }



    @Test
    public void testUpdateCaseDetailsCitizenUpdateOnDaRespondent() throws JsonProcessingException {
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("Test")
            .lastName("User")
            .user(User.builder()
                      .email("testparty@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .build();
        caseData = CaseData.builder()
            .respondentsFL401(partyDetails)
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(YesOrNo.Yes)
                                                                         .partyId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
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
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .build();
        updateCaseData = UpdateCaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.respondent)
            .build();
        userDetails = UserDetails.builder().build();
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseData);
        when(caseRepository.getCase(Mockito.anyString(), Mockito.anyString())).thenReturn(caseDetails);
        when(caseRepository.updateCase(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(caseDetails);
        when(idamClient.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(coreCaseDataApi.getCase(Mockito.any(),Mockito.any(), Mockito.any())).thenReturn(caseDetails);
        when(coreCaseDataService.startUpdate("", null, "", true)).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        when(coreCaseDataService.startUpdate(null, null, "", true)).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        CaseDetails caseDetailsAfterUpdate = caseService.updateCaseDetails(authToken, "123", "citizen-case-submit",updateCaseData);
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
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .build();
        caseData = CaseData.builder()
            .applicantsFL401(partyDetails)
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(YesOrNo.Yes)
                                                                         .partyId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
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
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .build();
        updateCaseData = UpdateCaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.applicant)
            .build();
        userDetails = UserDetails.builder().build();
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseData);
        when(caseRepository.getCase(Mockito.anyString(), Mockito.anyString())).thenReturn(caseDetails);
        when(caseRepository.updateCase(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(caseDetails);
        when(idamClient.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(coreCaseDataApi.getCase(Mockito.any(),Mockito.any(), Mockito.any())).thenReturn(caseDetails);
        when(coreCaseDataService.startUpdate("", null, "", true)).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        when(coreCaseDataService.startUpdate(null, null, "", true)).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        CaseDetails caseDetailsAfterUpdate = caseService.updateCaseDetails(authToken, "123", "citizen-case-submit",updateCaseData);
        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testValidateAccessCodeForInvalidCase() {

        when(caseService.getCase(authToken, caseId)).thenReturn(null);

        String isValid = caseService.validateAccessCode(authToken,s2sToken,caseId,accessCode);

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
        when(objectMapper.convertValue(stringObjectMap,CaseData.class)).thenReturn(caseData);

        String isValid = caseService.validateAccessCode(authToken,s2sToken,caseId,accessCode);

        assertEquals(INVALID, isValid);
    }
}
