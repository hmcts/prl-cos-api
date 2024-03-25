package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
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
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesNoNotSure;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.ServeOrderDetails;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocumentsManagement;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.CitizenSos;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StatementOfService;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.TestUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT_WITH_HWF;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseServiceTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "Bearer TestAuthToken";
    public static final String caseId = "1234567891234567";
    public static final String eventId = "1234567891234567";

    public static final String accessCode = "123456";
    public static final String INVALID = "Invalid";
    private final String eventName = "paymentSuccessCallback";
    private final String eventToken = "eventToken";

    private static final CaseData CASE_DATA = mock(CaseData.class);

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

    @Mock
    private UserService userService;

    private CaseData caseData;
    private CaseData caseData2;

    private CaseData caseData3;
    private CaseData caseDataWithOutPartyId;
    private CaseDetails caseDetails;
    private UserDetails userDetails;
    private Map<String, Object> caseDataMap;
    private PartyDetails partyDetails;
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
            .applicants(List.of(Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                    .value(partyDetails).build()))
            .respondents(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(YesOrNo.Yes)
                                                                         .partyId(testUuid)
                                                                         .accessCode("123").build()).build()))
            .build();

        caseData2 = CaseData.builder()
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .respondents(List.of(Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                     .value(partyDetails).build()))
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(YesOrNo.No)
                                                                         .partyId(testUuid)
                                                                         .accessCode("123").build()).build()))
            .build();

        caseData3 = CaseData.builder()
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .respondents(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .respondentsFL401(partyDetails)
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(YesOrNo.No)
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
                                        .solicitorRepresented(YesOrNo.Yes)
                                        .build())
                              .citizenSosObject(CitizenSos.builder()
                                                    .partiesServed("123,234,1234")
                                                    .build())
                              .build())
            .partyType(PartyEnum.applicant)
            .build();
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseData);
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
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseData2);
        CaseDetails caseDetailsAfterUpdate = caseService.updateCase(caseData2, "", "","","linkCase","123");
        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testupdateCaseRespondentNoPartyId() throws JsonProcessingException {
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseData3);
        CaseDetails caseDetailsAfterUpdate = caseService.updateCase(caseData2, "", "","","linkCase","123");
        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testupdateCaseApplicant() throws JsonProcessingException {
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
                                                                         .accessCode("1234").hasLinked("Yes").build()).build()))
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
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(null);
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
    @Ignore
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
        EventRequestData eventRequestData = EventRequestData.builder().build();
        when(caseService.getCase(authToken, caseId)).thenReturn(caseDetails);
        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseData);
        when(coreCaseDataService.eventRequest(CaseEvent.CITIZEN_CASE_WITHDRAW, userDetails.getId())).thenReturn(eventRequestData);        //When
        when(coreCaseDataService.startUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(
            StartEventResponse.builder().caseDetails(mock(CaseDetails.class)).build());
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(Mockito.any(), Mockito.any()))
            .thenReturn(caseDataContent);
        when(coreCaseDataService.submitUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(caseDetails);
        CaseDetails actualCaseDetails =  caseService.withdrawCase(caseData, caseId, authToken);
        //Then
        assertNotNull(actualCaseDetails);
    }


    @Test
    public void testUpdateCaseDetailsCitizenUpdateOnCaApplicant() throws JsonProcessingException {

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(coreCaseDataService.eventRequest(any(),any())).thenReturn(EventRequestData.builder().build());
        startEventResponse = StartEventResponse.builder().eventId(eventName)
            .caseDetails(caseDetails)
            .token(eventToken).build();
        when(coreCaseDataService.startUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(
            startEventResponse);
        User user1 = User.builder().idamId("123").build();
        PartyDetails applicant1 = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();
        PartyDetails applicant2 = PartyDetails.builder().email("test@hmcts.net").firstName("test").build();
        caseData = CaseData.builder()
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .build();

        when(CaseUtils.getCaseData(
            startEventResponse.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        caseDataMap = caseData.toMap(objectMapper);
        when(caseDataMock.toMap(any())).thenReturn(new HashMap<>());

        caseDetails = caseDetails.toBuilder()
            .data(caseDataMap)
            .id(123L)
            .state("SUBMITTED_PAID")
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .user(user1)
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .isAtAddressLessThan5YearsWithDontKnow(YesNoDontKnow.yes)
            .build();

        when(confidentialDetailsMapper.mapConfidentialData(
            Mockito.any(CaseData.class),
            Mockito.anyBoolean()
        )).thenReturn(caseDataMock);

        citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .partyDetails(respondent)
            .partyType(PartyEnum.applicant)
            .build();
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(Mockito.any(), Mockito.any()))
            .thenReturn(caseDataContent);
        when(applicationsTabService.updateCitizenPartiesTab(CASE_DATA)).thenReturn(applicaionFieldsMap);

        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseDataMock);

        when(coreCaseDataService.submitUpdate(
            any(),any(),any(),any(),anyBoolean())).thenReturn(
            CaseDetails.builder().build());
        CaseDetails caseDetailsAfterUpdate = caseService.updateCaseDetails(authToken, "123", "citizen-case-submit",
                                                                           citizenUpdatedCaseData
        );

        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testUpdateCaseDetailsCitizenUpdateOnCaRespondent() throws JsonProcessingException {

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(coreCaseDataService.eventRequest(any(),any())).thenReturn(EventRequestData.builder().build());
        startEventResponse = StartEventResponse.builder().eventId(eventName)
            .caseDetails(caseDetails)
            .token(eventToken).build();
        when(coreCaseDataService.startUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(
            startEventResponse);
        User user1 = User.builder().idamId("123").build();
        PartyDetails respondent1 = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();
        PartyDetails respondent2 = PartyDetails.builder().email("test@hmcts.net").firstName("test").build();
        caseData = CaseData.builder()
            .respondents(Arrays.asList(element(respondent1), element(respondent2)))
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.notSure).build())
            .statementOfService(StatementOfService.builder()

                                    .build())
            .build();

        when(CaseUtils.getCaseData(
            startEventResponse.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        caseDataMap = caseData.toMap(objectMapper);
        when(caseDataMock.toMap(any())).thenReturn(new HashMap<>());

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
            .address(Address.builder().addressLine1("teststreet").postCode("AP6 3EW").build())
            .build();
        citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.respondent)
            .build();
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(Mockito.any(), Mockito.any()))
            .thenReturn(caseDataContent);
        when(applicationsTabService.updateCitizenPartiesTab(CASE_DATA)).thenReturn(applicaionFieldsMap);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseDataMock);
        when(coreCaseDataService.submitUpdate(
            any(),any(),any(),any(),anyBoolean())).thenReturn(
            CaseDetails.builder().build());
        CaseDetails caseDetailsAfterUpdate = caseService.updateCaseDetails(authToken, "123", "citizen-case-submit",
                                                                           citizenUpdatedCaseData
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
        citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.applicant)
            .build();


        userDetails = UserDetails.builder().build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(caseRepository.getCase(authToken,"123")).thenReturn(caseDetails);
        when(caseRepository.updateCase(authToken, "123", caseData, CITIZEN_CASE_UPDATE)).thenReturn(caseDetails);

        Assert.assertThrows(RuntimeException.class, () -> caseService.updateCaseDetails(authToken, "123", "citizen-case-submit",
                                                                                        citizenUpdatedCaseData
        ));
    }



    @Test
    public void testUpdateCaseDetailsCitizenUpdateOnDaRespondent() throws JsonProcessingException {
        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(coreCaseDataService.eventRequest(any(),any())).thenReturn(EventRequestData.builder().build());
        startEventResponse = StartEventResponse.builder().eventId(eventName)
            .caseDetails(caseDetails)
            .token(eventToken).build();
        when(coreCaseDataService.startUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(
            startEventResponse);
        User user1 = User.builder().idamId("123").build();
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
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.notSure).build())
            .statementOfService(StatementOfService.builder()

                                    .build())
            .build();

        when(CaseUtils.getCaseData(
            startEventResponse.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        caseDataMap = caseData.toMap(objectMapper);
        when(caseDataMock.toMap(any())).thenReturn(new HashMap<>());

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
            .citizenSosObject(CitizenSos.builder().build())
            .build();
        citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.respondent)
            .build();
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(Mockito.any(), Mockito.any()))
            .thenReturn(caseDataContent);
        when(applicationsTabService.updateCitizenPartiesTab(CASE_DATA)).thenReturn(applicaionFieldsMap);

        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseDataMock);
        when(coreCaseDataService.submitUpdate(
            any(),any(),any(),any(),anyBoolean())).thenReturn(
            CaseDetails.builder().build());
        CaseDetails caseDetailsAfterUpdate = caseService.updateCaseDetails(authToken, "123", "citizen-case-submit",
                                                                           citizenUpdatedCaseData
        );
        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testUpdateCaseDetailsCitizenUpdateOnDaApplicant() throws JsonProcessingException {
        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(coreCaseDataService.eventRequest(any(),any())).thenReturn(EventRequestData.builder().build());
        startEventResponse = StartEventResponse.builder().eventId(eventName)
            .caseDetails(caseDetails)
            .token(eventToken).build();
        when(coreCaseDataService.startUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(
            startEventResponse);
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
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.notSure).build())
            .statementOfService(StatementOfService.builder()

                                    .build())
            .build();

        when(CaseUtils.getCaseData(
            startEventResponse.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        caseDataMap = caseData.toMap(objectMapper);
        when(caseDataMock.toMap(any())).thenReturn(new HashMap<>());

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
            .citizenSosObject(CitizenSos.builder().build())
            .build();
        citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.applicant)
            .build();
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(Mockito.any(), Mockito.any()))
            .thenReturn(caseDataContent);
        when(applicationsTabService.updateCitizenPartiesTab(CASE_DATA)).thenReturn(applicaionFieldsMap);

        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseDataMock);
        when(coreCaseDataService.submitUpdate(
            any(),any(),any(),any(),anyBoolean())).thenReturn(
            CaseDetails.builder().build());
        CaseDetails caseDetailsAfterUpdate = caseService.updateCaseDetails(authToken, "123", "citizen-case-submit",
                                                                           citizenUpdatedCaseData
        );
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

    @Test
    public void testValidateAccessCodeForToggleInvalidEmptyCaseInvites() {
        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantCaseName("test")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1234567891234567L)
            .data(stringObjectMap)
            .build();
        when(coreCaseDataApi.getCase(authToken, s2sToken, caseId)).thenReturn(caseDetails);
        when(objectMapper.convertValue(stringObjectMap,CaseData.class)).thenReturn(caseData);
        when(launchDarklyClient.isFeatureEnabled("citizen-allow-da-journey")).thenReturn(false);

        String isValid = caseService.validateAccessCode(authToken,s2sToken,caseId,accessCode);

        assertEquals(INVALID, isValid);
    }

    @Test
    public void testValidateAccessCodeForToggleInvalidWithCaseInvites() {
        List<CaseInvite> caseInvites = new ArrayList<>();
        caseInvites.add(CaseInvite.builder().partyId(testUuid).build());
        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantCaseName("test")
            .caseInvites(wrapElements(caseInvites)).build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1234567891234567L)
            .data(stringObjectMap)
            .build();
        when(coreCaseDataApi.getCase(authToken, s2sToken, caseId)).thenReturn(caseDetails);
        when(objectMapper.convertValue(stringObjectMap,CaseData.class)).thenReturn(caseData);
        when(launchDarklyClient.isFeatureEnabled("citizen-allow-da-journey")).thenReturn(false);

        String isValid = caseService.validateAccessCode(authToken,s2sToken,caseId,accessCode);

        assertEquals(INVALID, isValid);
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
        CaseDetails actualCaseDetails =  caseService.updateCase(caseData, authToken, s2sToken, caseId,
            CITIZEN_CASE_UPDATE.getValue(), accessCode);

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
        CaseDetails actualCaseDetails =  caseService.updateCase(caseData, authToken, s2sToken, caseId,
            CITIZEN_CASE_UPDATE.getValue(), accessCode);

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
        CaseDetails actualCaseDetails =  caseService.updateCase(caseData, authToken, s2sToken, caseId,
            CITIZEN_CASE_UPDATE.getValue(), accessCode);

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
        CaseDetails actualCaseDetails =  caseService.updateCase(caseData, authToken, s2sToken, caseId,
            CITIZEN_CASE_UPDATE.getValue(), accessCode);

        //Then
        assertThat(actualCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    public void testUpdateCaseSosWithCitizenDocs() {
        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(coreCaseDataService.eventRequest(any(),any())).thenReturn(EventRequestData.builder().build());
        startEventResponse = StartEventResponse.builder().eventId(eventName)
            .caseDetails(caseDetails)
            .token(eventToken).build();
        when(coreCaseDataService.startUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(
            startEventResponse);
        User user1 = User.builder().idamId("123").build();
        PartyDetails applicant1 = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test")
            .citizenSosObject(CitizenSos.builder().build()).build();
        PartyDetails applicant2 = PartyDetails.builder().email("test@hmcts.net").firstName("test")
            .citizenSosObject(CitizenSos.builder().build()).build();
        caseData = CaseData.builder()
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.notSure).build())
            .statementOfService(StatementOfService.builder()

                                    .build())
            .build();

        when(CaseUtils.getCaseData(
            startEventResponse.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        caseDataMap = caseData.toMap(objectMapper);
        when(caseDataMock.toMap(any())).thenReturn(new HashMap<>());

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
            .citizenSosObject(CitizenSos.builder().build())
            .build();
        citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.applicant)
            .build();
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(Mockito.any(), Mockito.any()))
            .thenReturn(caseDataContent);
        when(applicationsTabService.updateCitizenPartiesTab(CASE_DATA)).thenReturn(applicaionFieldsMap);

        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseDataMock);
        when(coreCaseDataService.submitUpdate(
            any(),any(),any(),any(),anyBoolean())).thenReturn(
            CaseDetails.builder().build());
        CaseDetails caseDetailsAfterUpdate = caseService.updateCaseDetails(authToken, "123",
                                                                           CaseEvent.CITIZEN_STATEMENT_OF_SERVICE.getValue(),
                                                                           citizenUpdatedCaseData
        );
        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testupdateCaseSosWithCitizenSosDocs() {
        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(coreCaseDataService.eventRequest(any(),any())).thenReturn(EventRequestData.builder().build());
        startEventResponse = StartEventResponse.builder().eventId(eventName)
            .caseDetails(caseDetails)
            .token(eventToken).build();
        when(coreCaseDataService.startUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(
            startEventResponse);
        User user1 = User.builder().idamId("123").build();
        PartyDetails applicant1 = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test")
            .citizenSosObject(CitizenSos.builder().build()).build();
        PartyDetails applicant2 = PartyDetails.builder().email("test@hmcts.net").firstName("test")
            .citizenSosObject(CitizenSos.builder().build()).build();
        caseData = CaseData.builder()
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.notSure).build())
            .statementOfService(StatementOfService.builder()

                                    .build())
            .build();

        when(CaseUtils.getCaseData(
            startEventResponse.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        caseDataMap = caseData.toMap(objectMapper);
        when(caseDataMock.toMap(any())).thenReturn(new HashMap<>());

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
            .citizenSosObject(CitizenSos.builder().build())
            .build();
        citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.applicant)
            .build();
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(Mockito.any(), Mockito.any()))
            .thenReturn(caseDataContent);
        when(applicationsTabService.updateCitizenPartiesTab(CASE_DATA)).thenReturn(applicaionFieldsMap);

        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseDataMock);
        when(coreCaseDataService.submitUpdate(
            any(),any(),any(),any(),anyBoolean())).thenReturn(
            CaseDetails.builder().build());
        CaseDetails caseDetailsAfterUpdate = caseService.updateCaseDetails(authToken, "123",
                                                                           CaseEvent.CITIZEN_STATEMENT_OF_SERVICE.getValue(),
                                                                           citizenUpdatedCaseData
        );
        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testFetchIdamAmRoles() {
        String emailId = "test@email.com";
        Map<String, String> amRoles = new HashMap<>();
        amRoles.put("amRoles","case-worker");
        Mockito.when(roleAssignmentService.fetchIdamAmRoles(authToken, emailId)).thenReturn(amRoles);
        Map<String, String> roles = caseService.fetchIdamAmRoles(authToken, emailId);
        Assert.assertFalse(roles.isEmpty());
    }


    @Test
    public void testUpdateCaseDetailsCitizenNew() {

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(coreCaseDataService.eventRequest(any(),any())).thenReturn(EventRequestData.builder().build());
        startEventResponse = StartEventResponse.builder().eventId(eventName)
            .caseDetails(caseDetails)
            .token(eventToken).build();
        when(coreCaseDataService.startUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(
            startEventResponse);
        User user1 = User.builder().idamId("123").build();
        PartyDetails applicant1 = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test")
            .citizenSosObject(CitizenSos.builder().build()).build();
        PartyDetails applicant2 = PartyDetails.builder().email("test@hmcts.net").firstName("test")
            .citizenSosObject(CitizenSos.builder().build()).build();
        caseData = CaseData.builder()
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.notSure).build())
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceAddRecipient(List.of(element(StmtOfServiceAddRecipient.builder().build())))
                                    .build())
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .build())
            .respondents(List.of(Element.<PartyDetails>builder().id(testUuid).value(partyDetails).build()))
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(YesOrNo.Yes)
                                                                         .partyId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                                                         .accessCode("123").build()).build()))
            .build();

        when(CaseUtils.getCaseData(
            startEventResponse.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        caseDataMap = caseData.toMap(objectMapper);
        when(caseDataMock.toMap(any())).thenReturn(new HashMap<>());

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
            .citizenSosObject(CitizenSos.builder().build())
            .build();
        citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.applicant)
            .build();
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(Mockito.any(), Mockito.any()))
            .thenReturn(caseDataContent);
        when(applicationsTabService.updateCitizenPartiesTab(CASE_DATA)).thenReturn(applicaionFieldsMap);

        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseDataMock);
        when(coreCaseDataService.submitUpdate(
            any(),any(),any(),any(),anyBoolean())).thenReturn(
            CaseDetails.builder().build());
        CaseDetails caseDetailsAfterUpdate = caseService.updateCaseDetails(authToken, "123",
                                                                           "citizenStatementOfService",
                                                                           citizenUpdatedCaseData
        );

        assertNotNull(caseDetailsAfterUpdate);
    }


    @Test
    public void testUpdateCaseDetailsCitizenNegativeScenarioWithNullPartyDetails() {

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(coreCaseDataService.eventRequest(any(),any())).thenReturn(EventRequestData.builder().build());
        startEventResponse = StartEventResponse.builder().eventId(eventName)
            .caseDetails(caseDetails)
            .token(eventToken).build();
        when(CaseUtils.getCaseData(
            startEventResponse.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        PartyDetails partyDetails1 = PartyDetails.builder()
            .firstName("Test")
            .lastName("User")
            .citizenSosObject(CitizenSos.builder().build())
            .build();
        citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.applicant)
            .build();
        assertThrows(RuntimeException.class, () -> caseService.updateCaseDetails(authToken, "123",
                                                                                 "citizenStatementOfService",
                                                                                 citizenUpdatedCaseData));
    }


    @Test
    public void testUpdateCaseDetailsCitizenNegativeScenarioWithNoC100CaseTypeOfApplication() {

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(coreCaseDataService.eventRequest(any(),any())).thenReturn(EventRequestData.builder().build());
        startEventResponse = StartEventResponse.builder().eventId(eventName)
            .caseDetails(caseDetails)
            .token(eventToken).build();
        when(coreCaseDataService.startUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(
            startEventResponse);
        User user1 = User.builder().idamId("123").build();
        PartyDetails applicant1 = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test")
            .citizenSosObject(CitizenSos.builder().build()).build();
        PartyDetails applicant2 = PartyDetails.builder().email("test@hmcts.net").firstName("test")
            .citizenSosObject(CitizenSos.builder().build()).build();
        caseData = CaseData.builder()
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .applicantsFL401(applicant1)
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.notSure).build())
            .statementOfService(StatementOfService.builder()

                                    .build())
            .build();

        when(CaseUtils.getCaseData(
            startEventResponse.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        caseDataMap = caseData.toMap(objectMapper);
        when(caseDataMock.toMap(any())).thenReturn(new HashMap<>());

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
            .citizenSosObject(CitizenSos.builder().build())
            .build();
        citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .partyDetails(partyDetails1)
            .partyType(PartyEnum.applicant)
            .build();
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(Mockito.any(), Mockito.any()))
            .thenReturn(caseDataContent);
        when(applicationsTabService.updateCitizenPartiesTab(CASE_DATA)).thenReturn(applicaionFieldsMap);

        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseDataMock);
        when(coreCaseDataService.submitUpdate(
            any(),any(),any(),any(),anyBoolean())).thenReturn(
            CaseDetails.builder().build());
        CaseDetails caseDetailsAfterUpdate = caseService.updateCaseDetails(authToken, "123",
                                                                           "confirmYourDetails",
                                                                           citizenUpdatedCaseData
        );

        assertNotNull(caseDetailsAfterUpdate);
    }


    @Test
    public void testUpdateCaseDetailsCitizenNewKeepDetailsPrivate() {

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(coreCaseDataService.eventRequest(any(),any())).thenReturn(EventRequestData.builder().build());
        startEventResponse = StartEventResponse.builder().eventId(eventName)
            .caseDetails(caseDetails)
            .token(eventToken).build();
        when(coreCaseDataService.startUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(
            startEventResponse);
        User user1 = User.builder().idamId("123").build();
        PartyDetails respondent1 = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();
        PartyDetails respondent2 = PartyDetails.builder().email("test@hmcts.net").firstName("test").build();
        caseData = CaseData.builder()
            .applicants(Arrays.asList(element(respondent1), element(respondent2)))
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.notSure).build())
            .statementOfService(StatementOfService.builder().build())
            .build();

        when(CaseUtils.getCaseData(
            startEventResponse.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        caseDataMap = caseData.toMap(objectMapper);
        when(caseDataMock.toMap(any())).thenReturn(new HashMap<>());

        caseDetails = caseDetails.toBuilder()
            .data(caseDataMap)
            .id(123L)
            .state("SUBMITTED_PAID")
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .user(user1)
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .isAtAddressLessThan5YearsWithDontKnow(YesNoDontKnow.yes)
            .build();

        when(confidentialDetailsMapper.mapConfidentialData(
            Mockito.any(CaseData.class),
            Mockito.anyBoolean()
        )).thenReturn(caseDataMock);

        citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .partyDetails(respondent)
            .partyType(PartyEnum.applicant)
            .build();
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(Mockito.any(), Mockito.any()))
            .thenReturn(caseDataContent);
        when(applicationsTabService.updateCitizenPartiesTab(CASE_DATA)).thenReturn(applicaionFieldsMap);

        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseDataMock);

        when(coreCaseDataService.submitUpdate(
            any(),any(),any(),any(),anyBoolean())).thenReturn(
            CaseDetails.builder().build());
        CaseDetails caseDetailsAfterUpdate = caseService.updateCaseDetails(authToken, "123",
                                                                           "keepYourDetailsPrivate",
                                                                           citizenUpdatedCaseData
        );

        assertNotNull(caseDetailsAfterUpdate);
    }

    @Test
    public void testGetCitizenDocuments() {
        //Given
        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder()
            .miamCertificateDocument(Document.builder().build())
            .documentParty("applicant")
            .categoryId("miamCertificate")
            .uploadedBy("test")
            .uploaderRole(CITIZEN)
            .uploadedByIdamId("00000000-0000-0000-0000-000000000000")
            .build();
        caseData = caseData.toBuilder()
            .reviewDocuments(ReviewDocuments.builder()
                                 .legalProfUploadDocListDocTab(List.of(element(quarantineLegalDoc)))
                                 .cafcassUploadDocListDocTab(List.of(element(quarantineLegalDoc)))
                                 .courtStaffUploadDocListDocTab(List.of(element(quarantineLegalDoc)))
                                 .citizenUploadedDocListDocTab(List.of(element(quarantineLegalDoc)))
                                 .confidentialDocuments(List.of(element(quarantineLegalDoc)))
                                 .restrictedDocuments(List.of(element(quarantineLegalDoc)))
                                 .build())
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .citizenQuarantineDocsList(List.of(element(quarantineLegalDoc)))
                                           .build())
            .build();
        userDetails = UserDetails.builder()
            .id("00000000-0000-0000-0000-000000000000")
            .roles(List.of(Roles.CITIZEN.getValue())).build();
        Map<String, Object> map = new HashMap<>();
        map.put("miamCertificateDocument", quarantineLegalDoc);

        //When
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);
        when(objectMapper.convertValue(quarantineLegalDoc, Map.class)).thenReturn(map);
        when(objectMapper.convertValue(map.get("miamCertificateDocument"), Document.class))
            .thenReturn(quarantineLegalDoc.getMiamCertificateDocument());

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertFalse(citizenDocumentsManagement.getCitizenDocuments().isEmpty());
        assertEquals(7, citizenDocumentsManagement.getCitizenDocuments().size());
    }

    @Test
    public void testEmptyCitizenDocumentsWhenNoDocs() {
        //Given
        caseData = caseData.toBuilder().build();
        userDetails = UserDetails.builder()
            .id("00000000-0000-0000-0000-000000000000")
            .roles(List.of(Roles.CITIZEN.getValue())).build();
        Map<String, Object> map = new HashMap<>();

        //When
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(citizenDocumentsManagement.getCitizenDocuments().isEmpty());
    }

    @Test
    public void testFilterNonAccessibleCitizenDocuments() {
        //Given
        QuarantineLegalDoc cafcassDoc = QuarantineLegalDoc.builder()
            .uploaderRole(CAFCASS)
            .build();
        QuarantineLegalDoc otherPartyDoc = QuarantineLegalDoc.builder()
            .uploaderRole(CITIZEN)
            .uploadedByIdamId("00000000-0000-0000-0000-000000000001")
            .build();
        caseData = caseData.toBuilder()
            .reviewDocuments(ReviewDocuments.builder()
                                 .confidentialDocuments(List.of(element(otherPartyDoc)))
                                 .restrictedDocuments(List.of(element(cafcassDoc)))
                                 .build())
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .citizenQuarantineDocsList(List.of(element(otherPartyDoc)))
                                           .build())
            .build();
        userDetails = UserDetails.builder()
            .id("00000000-0000-0000-0000-000000000000")
            .roles(List.of(Roles.CITIZEN.getValue()))
            .build();

        //When
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(citizenDocumentsManagement.getCitizenDocuments().isEmpty());
    }

    @Test
    public void testGetCitizenApplicantOrdersC100() {
        //Given
        ServedParties servedParties = ServedParties.builder()
            .partyId("00000000-0000-0000-0000-000000000000")
            .build();
        OrderDetails orderDetails = OrderDetails.builder()
            .orderDocument(Document.builder().build())
            .orderDocumentWelsh(Document.builder().build())
            .serveOrderDetails(ServeOrderDetails.builder()
                                   .servedParties(List.of(element(servedParties)))
                                   .build())
            .otherDetails(OtherOrderDetails.builder().createdBy("test").build())
            .build();
        partyDetails = partyDetails.toBuilder()
            .user(User.builder()
                      .idamId("00000000-0000-0000-0000-000000000000").build())
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .orderCollection(List.of(element(orderDetails)))
            .applicants(List.of(element(testUuid, partyDetails)))
            .build();
        userDetails = UserDetails.builder()
            .id("00000000-0000-0000-0000-000000000000")
            .roles(List.of(Roles.CITIZEN.getValue())).build();

        //When
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertFalse(citizenDocumentsManagement.getCitizenOrders().isEmpty());
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
    }

    @Test
    public void testGetCitizenRespondentOrdersC100() {
        //Given
        ServedParties servedParties = ServedParties.builder()
            .partyId("00000000-0000-0000-0000-000000000000")
            .build();
        OrderDetails orderDetails = OrderDetails.builder()
            .orderDocument(Document.builder().build())
            .orderDocumentWelsh(Document.builder().build())
            .serveOrderDetails(ServeOrderDetails.builder()
                                   .servedParties(List.of(element(servedParties)))
                                   .build())
            .otherDetails(OtherOrderDetails.builder().createdBy("test").build())
            .build();
        partyDetails = partyDetails.toBuilder()
            .user(User.builder()
                      .idamId("00000000-0000-0000-0000-000000000000").build())
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .orderCollection(List.of(element(orderDetails)))
            .respondents(List.of(element(testUuid, partyDetails)))
            .build();
        userDetails = UserDetails.builder()
            .id("00000000-0000-0000-0000-000000000000")
            .roles(List.of(Roles.CITIZEN.getValue())).build();

        //When
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertFalse(citizenDocumentsManagement.getCitizenOrders().isEmpty());
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
    }

    @Test
    public void testGetCitizenApplicantOrdersFL401() {
        //Given
        ServedParties servedParties = ServedParties.builder()
            .partyId("00000000-0000-0000-0000-000000000000")
            .build();
        OrderDetails orderDetails = OrderDetails.builder()
            .orderDocument(Document.builder().build())
            .orderDocumentWelsh(Document.builder().build())
            .serveOrderDetails(ServeOrderDetails.builder()
                                   .servedParties(List.of(element(servedParties)))
                                   .build())
            .otherDetails(OtherOrderDetails.builder().createdBy("test").build())
            .build();
        partyDetails = partyDetails.toBuilder()
            .partyId(testUuid)
            .user(User.builder()
                      .idamId("00000000-0000-0000-0000-000000000000").build())
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("FL401")
            .orderCollection(List.of(element(orderDetails)))
            .applicantsFL401(partyDetails)
            .build();
        userDetails = UserDetails.builder()
            .id("00000000-0000-0000-0000-000000000000")
            .roles(List.of(Roles.CITIZEN.getValue())).build();

        //When
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertFalse(citizenDocumentsManagement.getCitizenOrders().isEmpty());
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
    }

    @Test
    public void testGetCitizenRespondentOrdersFL401() {
        //Given
        ServedParties servedParties = ServedParties.builder()
            .partyId("00000000-0000-0000-0000-000000000000")
            .build();
        OrderDetails orderDetails = OrderDetails.builder()
            .orderDocument(Document.builder().build())
            .orderDocumentWelsh(Document.builder().build())
            .serveOrderDetails(ServeOrderDetails.builder()
                                   .servedParties(List.of(element(servedParties)))
                                   .build())
            .otherDetails(OtherOrderDetails.builder().createdBy("test").build())
            .build();
        partyDetails = partyDetails.toBuilder()
            .partyId(testUuid)
            .user(User.builder()
                      .idamId("00000000-0000-0000-0000-000000000000").build())
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("FL401")
            .orderCollection(List.of(element(orderDetails)))
            .applicantsFL401(PartyDetails.builder().build())
            .respondentsFL401(partyDetails)
            .build();
        userDetails = UserDetails.builder()
            .id("00000000-0000-0000-0000-000000000000")
            .roles(List.of(Roles.CITIZEN.getValue())).build();

        //When
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertFalse(citizenDocumentsManagement.getCitizenOrders().isEmpty());
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
    }
}
