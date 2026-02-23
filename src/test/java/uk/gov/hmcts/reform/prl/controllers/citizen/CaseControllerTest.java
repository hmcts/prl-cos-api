package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CitizenCaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.UiCitizenCaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.TASK_LIST_V3_FLAG;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseControllerTest {

    @InjectMocks
    private CaseController caseController;

    @Mock
    private CaseService caseService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    ConfidentialDetailsMapper confidentialDetailsMapper;

    @Mock
    HearingService hearingService;

    @Mock
    AllTabServiceImpl allTabsService;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private UserInfo userInfo;

    private CaseData caseData;
    Address address;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String SERV_AUTH_TOKEN = "Bearer TestServToken";
    public static final String CITIZEN_ROLE = "citizen";
    public static final String CASEWORKER_ROLE = "caseworker";
    private static final String INVALID_ROLE = "Invalid Role on User";
    private static final String INVALID_CLIENT = "Invalid Client";
    private static final String TEST_CASE_ID = "1234567891234567";

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
    }

    @Test
    public void testGetCase() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn(SERV_AUTH_TOKEN);

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseService.getCase(AUTH_TOKEN, TEST_CASE_ID)).thenReturn(caseDetails);
        when(authTokenGenerator.generate()).thenReturn("servAuthToken");
        when(userInfo.getRoles()).thenReturn(List.of(CITIZEN_ROLE));
        when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Optional.of(userInfo));
        when(authorisationService.authoriseService(SERV_AUTH_TOKEN)).thenReturn(true);
        UiCitizenCaseData caseData1 = caseController.getCase(TEST_CASE_ID, AUTH_TOKEN, SERV_AUTH_TOKEN);
        assertEquals(caseData.getApplicantCaseName(), caseData1.getCaseData().getApplicantCaseName());

    }

    @Test
    public void testGetCaseInvalidUserRole() {
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn(SERV_AUTH_TOKEN);
        when(userInfo.getRoles()).thenReturn(List.of(CASEWORKER_ROLE));
        when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Optional.of(userInfo));

        assertThrows(INVALID_ROLE, ResponseStatusException.class,
                     () -> caseController.getCase(TEST_CASE_ID, AUTH_TOKEN, SERV_AUTH_TOKEN));
    }

    @Test
    public void testGetCaseInvalidClient() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(false);

        assertThrows(INVALID_CLIENT, ResponseStatusException.class,
                     () -> caseController.getCase(TEST_CASE_ID, AUTH_TOKEN, SERV_AUTH_TOKEN));
    }

    @Test
    public void testGetCaseWithHearing() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();
        String caseId = "1234567891234567";

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(true);
        when(userInfo.getRoles()).thenReturn(List.of(CITIZEN_ROLE));
        when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Optional.of(userInfo));

        when(caseService.getCaseWithHearing(AUTH_TOKEN, caseId, "test"))
            .thenReturn(CaseDataWithHearingResponse.builder().build());

        assertNotNull(caseController.retrieveCaseWithHearing(caseId, "test", AUTH_TOKEN, SERV_AUTH_TOKEN));
    }

    @Test
    public void testGetCaseWithHearingInvalidRole() {
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(true);
        when(userInfo.getRoles()).thenReturn(List.of(CASEWORKER_ROLE));
        when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Optional.of(userInfo));


        assertThrows(INVALID_ROLE, ResponseStatusException.class,
                     () -> caseController.retrieveCaseWithHearing(TEST_CASE_ID, "test", AUTH_TOKEN, SERV_AUTH_TOKEN));
    }

    @Test
    public void testGetCaseWithHearingInvalidClient() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(false);
        assertThrows(INVALID_CLIENT, ResponseStatusException.class,
                     () -> caseController.retrieveCaseWithHearing(TEST_CASE_ID, "test", AUTH_TOKEN, SERV_AUTH_TOKEN));
    }

    @Test
    public void testCitizenUpdateCase() throws JsonProcessingException {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .c100RebuildData(C100RebuildData.builder().c100RebuildApplicantDetails("").build())
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn(SERV_AUTH_TOKEN);
        when(userInfo.getRoles()).thenReturn(List.of(CITIZEN_ROLE));
        when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Optional.of(userInfo));

        address = Address.builder()
            .addressLine1("AddressLine1")
            .postTown("Xyz town")
            .postCode("AB1 2YZ")
            .build();

        List<Element<ApplicantConfidentialityDetails>> expectedOutput = List
            .of(Element.<ApplicantConfidentialityDetails>builder()
                    .value(ApplicantConfidentialityDetails.builder()
                               .firstName("ABC 1")
                               .lastName("XYZ 2")
                               .email("abc1@xyz.com")
                               .phoneNumber("09876543211")
                               .address(address)
                               .build()).build());

        CaseData updatedCasedata = CaseData.builder()
            .applicantCaseName("test")
            .respondentConfidentialDetails(expectedOutput)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();


        String eventId = "e3ceb507-0137-43a9-8bd3-85dd23720648";
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(confidentialDetailsMapper.mapConfidentialData(caseData, true)).thenReturn(updatedCasedata);
        when(authTokenGenerator.generate()).thenReturn("TestToken");
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(true);
        when(caseService.updateCase(caseData, AUTH_TOKEN, TEST_CASE_ID, eventId
        )).thenReturn(caseDetails);
        CaseData caseData1 = caseController.updateCase(
            caseData,
            TEST_CASE_ID,
            eventId,
            AUTH_TOKEN,
            SERV_AUTH_TOKEN,
            "testAccessCode"
        );
        assertEquals(caseData.getApplicantCaseName(), caseData1.getApplicantCaseName());

    }

    @Test
    public void testUpdateCaseInvalidRole() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(true);
        when(userInfo.getRoles()).thenReturn(List.of(CASEWORKER_ROLE));
        when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Optional.of(userInfo));

        assertThrows(INVALID_ROLE, ResponseStatusException.class,
                     () -> caseController.updateCase(caseData, TEST_CASE_ID,
                                                     "test", AUTH_TOKEN, SERV_AUTH_TOKEN, "test"));
    }

    @Test
    public void testUpdateCaseInvalidClient() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(false);
        assertThrows(INVALID_CLIENT, ResponseStatusException.class,
                     () -> caseController.updateCase(caseData, TEST_CASE_ID,
                                                     "test", AUTH_TOKEN, SERV_AUTH_TOKEN, "test"));
    }

    @Test
    public void testCitizenRetrieveCases() {

        List<CaseData> caseDataList = new ArrayList<>();

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();

        caseDataList.add(CaseData.builder()
                             .id(1234567891234567L)
                             .applicantCaseName("test")
                             .build());

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(true);
        when(userInfo.getRoles()).thenReturn(List.of(CITIZEN_ROLE));
        when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Optional.of(userInfo));

        List<CaseDetails> caseDetails = new ArrayList<>();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        caseDetails.add(CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build());

        String userId = "12345";
        String role = "test role";

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseService.retrieveCases(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(caseDataList);
        List<CaseData> caseDataList1 = caseController.retrieveCases(role, userId, AUTH_TOKEN, SERV_AUTH_TOKEN);
        assertNotNull(caseDataList1);

    }

    @Test
    public void testRetrieveCaseInvalidClient() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(false);
        assertThrows(INVALID_CLIENT, ResponseStatusException.class,
                     () -> caseController.retrieveCases(TEST_CASE_ID, TEST_CASE_ID, AUTH_TOKEN, SERV_AUTH_TOKEN));
    }

    @Test
    public void testretrieveCitizenCases() {
        List<CaseData> caseDataList = new ArrayList<>();

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();

        caseDataList.add(CaseData.builder()
                             .id(1234567891234567L)
                             .applicantCaseName("test")
                             .build());

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(true);
        when(userInfo.getRoles()).thenReturn(List.of(CITIZEN_ROLE));
        when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Optional.of(userInfo));

        List<CaseDetails> caseDetails = new ArrayList<>();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        caseDetails.add(CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build());

        List<CitizenCaseData> citizenCaseDataList;

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseService.retrieveCases(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(caseDataList);
        citizenCaseDataList = caseController.retrieveCitizenCases(AUTH_TOKEN, SERV_AUTH_TOKEN);
        assertNotNull(citizenCaseDataList);
    }

    @Test
    public void testRetrieveCitizenCaseInvalidRole() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(true);
        when(userInfo.getRoles()).thenReturn(List.of(CASEWORKER_ROLE));
        when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Optional.of(userInfo));

        assertThrows(INVALID_ROLE, ResponseStatusException.class,
                     () -> caseController.retrieveCitizenCases(AUTH_TOKEN, SERV_AUTH_TOKEN));
    }

    @Test
    public void testRetrieveCitizenCaseInvalidClient() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(false);
        assertThrows(INVALID_CLIENT, ResponseStatusException.class,
                     () -> caseController.retrieveCitizenCases(AUTH_TOKEN, SERV_AUTH_TOKEN));
    }

    @Test
    public void shouldCreateCase() {
        //Given
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .noOfDaysRemainingToSubmitCase(PrlAppsConstants.CASE_SUBMISSION_THRESHOLD)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseService.createCase(caseData, AUTH_TOKEN)).thenReturn(caseDetails);
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(true);
        when(userInfo.getRoles()).thenReturn(List.of(CITIZEN_ROLE));
        when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Optional.of(userInfo));
        when(authTokenGenerator.generate()).thenReturn(SERV_AUTH_TOKEN);
        //When
        CaseData actualCaseData = caseController.createCase(AUTH_TOKEN, SERV_AUTH_TOKEN, caseData);

        //Then
        assertThat(actualCaseData).isEqualTo(caseData);
    }

    @Test
    public void testCreateCaseInvalidRole() {
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(true);
        when(userInfo.getRoles()).thenReturn(List.of(CASEWORKER_ROLE));
        when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Optional.of(userInfo));

        assertThrows(INVALID_ROLE, ResponseStatusException.class,
                     () -> caseController.createCase(AUTH_TOKEN, SERV_AUTH_TOKEN, caseData));
    }

    @Test
    public void testCreateCaseInvalidClient() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(false);
        assertThrows(INVALID_CLIENT, ResponseStatusException.class,
                     () -> caseController.createCase(AUTH_TOKEN, SERV_AUTH_TOKEN, caseData));
    }

    @Test
    public void testGetAllHearingsForCitizenCase() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(true);
        when(userInfo.getRoles()).thenReturn(List.of(CITIZEN_ROLE));
        when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Optional.of(userInfo));

        Mockito.when(hearingService.getHearings(AUTH_TOKEN, TEST_CASE_ID)).thenReturn(
            Hearings.hearingsWith().build());

        Hearings hearingForCase = caseController.getAllHearingsForCitizenCase(
            AUTH_TOKEN, SERV_AUTH_TOKEN, TEST_CASE_ID);
        Assert.assertNotNull(hearingForCase);
    }

    @Test
    public void testGetAllHearingsForCaseInvalidRole() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(true);
        when(userInfo.getRoles()).thenReturn(List.of(CASEWORKER_ROLE));
        when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Optional.of(userInfo));

        assertThrows(INVALID_ROLE, ResponseStatusException.class,
                     () -> caseController.getAllHearingsForCitizenCase(AUTH_TOKEN, SERV_AUTH_TOKEN, "test"));
    }

    @Test
    public void testGetAllHearingsForCaseInvalidClient() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(false);
        assertThrows(INVALID_CLIENT, ResponseStatusException.class,
                     () -> caseController.getAllHearingsForCitizenCase(AUTH_TOKEN, SERV_AUTH_TOKEN, "test"));
    }

    @Test
    public void testFetchIdamAmRoles() {
        String emailId = "test@email.com";
        Map<String, String> amRoles = new HashMap<>();
        amRoles.put("amRoles","case-worker");
        Mockito.when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Optional.of(userInfo));
        Mockito.when(caseService.fetchIdamAmRoles(AUTH_TOKEN, emailId)).thenReturn(amRoles);

        Map<String, String> roles = caseController.fetchIdamAmRoles(
            AUTH_TOKEN, emailId);
        Assert.assertFalse(roles.isEmpty());
    }

    @Test
    public void testFetchIdamAmRolesFails() {

        String emailId = "test@email.com";
        Map<String, String> amRoles = new HashMap<>();
        amRoles.put("amRoles","case-worker");
        Mockito.when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Optional.empty());
        Mockito.when(caseService.fetchIdamAmRoles(AUTH_TOKEN, emailId)).thenReturn(amRoles);


        assertThrows(INVALID_CLIENT, ResponseStatusException.class,
                    () -> caseController.fetchIdamAmRoles(AUTH_TOKEN, emailId));
    }

    @Test
    public void shouldCreateC100Case() {
        //Given
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .noOfDaysRemainingToSubmitCase(PrlAppsConstants.CASE_SUBMISSION_THRESHOLD)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseService.createCase(caseData, AUTH_TOKEN)).thenReturn(caseDetails);
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERV_AUTH_TOKEN)).thenReturn(true);
        when(userInfo.getRoles()).thenReturn(List.of(CITIZEN_ROLE));
        when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Optional.of(userInfo));
        when(authTokenGenerator.generate()).thenReturn(SERV_AUTH_TOKEN);
        when(launchDarklyClient.isFeatureEnabled(TASK_LIST_V3_FLAG)).thenReturn(true);
        //When
        CaseData actualCaseData = caseController.createCase(AUTH_TOKEN, SERV_AUTH_TOKEN, caseData);

        //Then
        assertThat(actualCaseData).isEqualTo(caseData);
    }

}
