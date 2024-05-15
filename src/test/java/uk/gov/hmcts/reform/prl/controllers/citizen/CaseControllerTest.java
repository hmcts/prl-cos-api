package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javassist.NotFoundException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CitizenCaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

    private CaseData caseData;
    Address address;
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private CitizenUpdatedCaseData citizenUpdatedCaseData;
    public static final String authToken = "Bearer TestAuthToken";
    public static final String servAuthToken = "Bearer TestServToken";

    @BeforeEach
    public void setUp() {
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

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn(servAuthToken);

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        String caseId = "1234567891234567";
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseService.getCase(authToken, caseId)).thenReturn(caseDetails);
        when(authTokenGenerator.generate()).thenReturn("servAuthToken");
        when(authorisationService.authoriseUser(authToken)).thenReturn(true);
        when(authorisationService.authoriseService(servAuthToken)).thenReturn(true);
        CaseData caseData1 = caseController.getCase(caseId, authToken, servAuthToken);
        assertEquals(caseData.getApplicantCaseName(), caseData1.getApplicantCaseName());

    }

    @Test(expected = RuntimeException.class)
    public void testGetCaseInvalidClient() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(false);

        String caseId = "1234567891234567";
        caseController.getCase(caseId, authToken, servAuthToken);
    }

    @Test
    public void testGetCaseWithHearing() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();
        String caseId = "1234567891234567";

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(true);
        when(caseService.getCaseWithHearing(authToken, caseId, "test")).thenReturn(CaseDataWithHearingResponse.builder().build());


        assertNotNull(caseController.retrieveCaseWithHearing(caseId, "test", authToken, servAuthToken));
    }

    @Test(expected = RuntimeException.class)
    public void testGetCaseWithHearingInvalidClient() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();
        String caseId = "1234567891234567";

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(false);
        caseController.retrieveCaseWithHearing(caseId, "test", authToken, servAuthToken);
    }

    @Test
    public void testCitizenUpdateCase() throws JsonProcessingException, NotFoundException {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn(servAuthToken);


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


        String caseId = "1234567891234567";
        String eventId = "e3ceb507-0137-43a9-8bd3-85dd23720648";
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(confidentialDetailsMapper.mapConfidentialData(caseData, true)).thenReturn(updatedCasedata);
        when(authTokenGenerator.generate()).thenReturn("TestToken");
        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(true);
        when(caseService.updateCase(caseData, authToken, caseId, eventId
        )).thenReturn(caseDetails);
        CaseData caseData1 = caseController.updateCase(
            caseData,
            caseId,
            eventId,
            authToken,
            servAuthToken,
            "testAccessCode"
        );
        assertEquals(caseData.getApplicantCaseName(), caseData1.getApplicantCaseName());

    }

    @Test(expected = RuntimeException.class)
    public void testUpdateCaseInvalidClient() throws JsonProcessingException {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();
        String caseId = "1234567891234567";

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(false);
        caseController.updateCase(caseData, caseId, "test", authToken, servAuthToken, "test");
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

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(true);

        List<CaseDetails> caseDetails = new ArrayList<>();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        caseDetails.add(CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build());

        String userId = "12345";
        String role = "test role";

        List<CaseData> caseDataList1 = new ArrayList<>();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseService.retrieveCases(authToken, servAuthToken)).thenReturn(caseDataList);
        caseDataList1 = caseController.retrieveCases(role, userId, authToken, servAuthToken);
        assertNotNull(caseDataList1);

    }

    @Test(expected = RuntimeException.class)
    public void testRetrieveCaseInvalidClient() {

        String caseId = "1234567891234567";

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(false);
        caseController.retrieveCases(caseId, caseId, authToken, servAuthToken);
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

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(true);

        List<CaseDetails> caseDetails = new ArrayList<>();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        caseDetails.add(CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build());

        List<CitizenCaseData> citizenCaseDataList = new ArrayList<>();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseService.retrieveCases(authToken, servAuthToken)).thenReturn(caseDataList);
        citizenCaseDataList = caseController.retrieveCitizenCases(authToken, servAuthToken);
        assertNotNull(citizenCaseDataList);
    }

    @Test(expected = RuntimeException.class)
    public void testRetrieveCitizenCaseInvalidClient() {

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(false);
        caseController.retrieveCitizenCases(authToken, servAuthToken);
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

        Mockito.when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        Mockito.when(caseService.createCase(caseData, authToken)).thenReturn(caseDetails);
        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(true);
        Mockito.when(authTokenGenerator.generate()).thenReturn(servAuthToken);
        //When
        CaseData actualCaseData = caseController.createCase(authToken, servAuthToken, caseData);

        //Then
        assertThat(actualCaseData).isEqualTo(caseData);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateCaseInvalidClient() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(false);
        caseController.createCase(authToken, servAuthToken, caseData);
    }

    @Test
    public void testGetAllHearingsForCitizenCase() throws IOException {
        String caseId = "1234567891234567";
        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(true);

        Mockito.when(hearingService.getHearings(authToken, caseId)).thenReturn(
            Hearings.hearingsWith().build());

        Hearings hearingForCase = caseController.getAllHearingsForCitizenCase(
            authToken, servAuthToken, caseId);
        Assert.assertNotNull(hearingForCase);
    }

    @Test(expected = RuntimeException.class)
    public void testGetAllHearingsForCaseInvalidClient() {

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(false);
        caseController.getAllHearingsForCitizenCase(authToken, servAuthToken, "test");
    }

    @Test
    public void testFetchIdamAmRoles() throws IOException {
        String emailId = "test@email.com";
        Map<String, String> amRoles = new HashMap<>();
        amRoles.put("amRoles","case-worker");
        Mockito.when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        Mockito.when(caseService.fetchIdamAmRoles(authToken, emailId)).thenReturn(amRoles);

        Map<String, String> roles = caseController.fetchIdamAmRoles(
            authToken, emailId);
        Assert.assertFalse(roles.isEmpty());
    }

    @Test
    public void testFetchIdamAmRolesFails() throws IOException {

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Invalid Client");

        String emailId = "test@email.com";
        Map<String, String> amRoles = new HashMap<>();
        amRoles.put("amRoles","case-worker");
        Mockito.when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.FALSE);
        Mockito.when(caseService.fetchIdamAmRoles(authToken, emailId)).thenReturn(amRoles);

        Map<String, String> roles = caseController.fetchIdamAmRoles(
            authToken, emailId);
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

        Mockito.when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        Mockito.when(caseService.createCase(caseData, authToken)).thenReturn(caseDetails);
        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(true);
        Mockito.when(authTokenGenerator.generate()).thenReturn(servAuthToken);
        Mockito.when(launchDarklyClient.isFeatureEnabled(TASK_LIST_V3_FLAG)).thenReturn(true);
        //When
        CaseData actualCaseData = caseController.createCase(authToken, servAuthToken, caseData);

        //Then
        assertThat(actualCaseData).isEqualTo(caseData);
    }

}
