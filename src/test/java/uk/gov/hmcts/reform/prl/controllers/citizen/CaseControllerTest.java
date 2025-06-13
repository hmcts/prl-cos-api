package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.TASK_LIST_V3_FLAG;

@ExtendWith(MockitoExtension.class)
class CaseControllerTest {

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

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S2S_TOKEN = "Bearer TestServToken";

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testGetCase() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        String caseId = "1234567891234567";
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseService.getCase(AUTH_TOKEN, caseId)).thenReturn(caseDetails);
        UiCitizenCaseData caseData1 = caseController.getCase(caseId, AUTH_TOKEN, S2S_TOKEN);
        assertEquals(caseData.getApplicantCaseName(), caseData1.getCaseData().getApplicantCaseName());
    }

    @Test
    void testGetCaseInvalidClient() {
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        String caseId = "1234567891234567";

        assertThrows(RuntimeException.class, () -> {
            caseController.getCase(caseId, AUTH_TOKEN, S2S_TOKEN);
        });
    }

    @Test
    void testGetCaseWithHearing() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();
        String caseId = "1234567891234567";

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        when(caseService.getCaseWithHearing(AUTH_TOKEN, caseId, "test")).thenReturn(CaseDataWithHearingResponse.builder().build());


        assertNotNull(caseController.retrieveCaseWithHearing(caseId, "test", AUTH_TOKEN, S2S_TOKEN));
    }

    @Test
    void testGetCaseWithHearingInvalidClient() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();
        String caseId = "1234567891234567";

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            caseController.retrieveCaseWithHearing(caseId, "test", AUTH_TOKEN, S2S_TOKEN);
        });
    }

    @Test
    void testCitizenUpdateCase() throws JsonProcessingException {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .c100RebuildData(C100RebuildData.builder().c100RebuildApplicantDetails("").build())
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);

        Address address = Address.builder()
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
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        when(caseService.updateCase(caseData, AUTH_TOKEN, caseId, eventId
        )).thenReturn(caseDetails);
        CaseData caseData1 = caseController.updateCase(
            caseData,
            caseId,
            eventId,
            AUTH_TOKEN,
            S2S_TOKEN,
            "testAccessCode"
        );
        assertEquals(caseData.getApplicantCaseName(), caseData1.getApplicantCaseName());

    }

    @Test
    void testUpdateCaseInvalidClient() {
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();
        String caseId = "1234567891234567";

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            caseController.updateCase(caseData, caseId, "test", AUTH_TOKEN, S2S_TOKEN, "test");
        });
    }

    @Test
    void testCitizenRetrieveCases() {

        List<CaseData> caseDataList = new ArrayList<>();

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();

        caseDataList.add(CaseData.builder()
                             .id(1234567891234567L)
                             .applicantCaseName("test")
                             .build());

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);


        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseService.retrieveCases(AUTH_TOKEN, S2S_TOKEN)).thenReturn(caseDataList);

        String userId = "12345";
        String role = "test role";
        List<CaseData> caseDataList1 = caseController.retrieveCases(role, userId, AUTH_TOKEN, S2S_TOKEN);
        assertNotNull(caseDataList1);

    }

    @Test
    void testRetrieveCaseInvalidClient() {
        String caseId = "1234567891234567";

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            caseController.retrieveCases(caseId, caseId, AUTH_TOKEN, S2S_TOKEN);
        });
    }

    @Test
    void testretrieveCitizenCases() {
        List<CaseData> caseDataList = new ArrayList<>();

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();

        caseDataList.add(CaseData.builder()
                             .id(1234567891234567L)
                             .applicantCaseName("test")
                             .build());

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);

        List<CaseDetails> caseDetails = new ArrayList<>();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        caseDetails.add(CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseService.retrieveCases(AUTH_TOKEN, S2S_TOKEN)).thenReturn(caseDataList);

        List<CitizenCaseData> citizenCaseDataList = caseController.retrieveCitizenCases(AUTH_TOKEN, S2S_TOKEN);
        assertNotNull(citizenCaseDataList);
    }

    @Test
    void testRetrieveCitizenCaseInvalidClient() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            caseController.retrieveCitizenCases(AUTH_TOKEN, S2S_TOKEN);
        });
    }

    @Test
    void shouldCreateCase() {
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
        Mockito.when(caseService.createCase(caseData, AUTH_TOKEN)).thenReturn(caseDetails);
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);

        //When
        CaseData actualCaseData = caseController.createCase(AUTH_TOKEN, S2S_TOKEN, caseData);

        //Then
        assertThat(actualCaseData).isEqualTo(caseData);
    }

    @Test
    void testCreateCaseInvalidClient() {
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            caseController.createCase(AUTH_TOKEN, S2S_TOKEN, caseData);
        });
    }

    @Test
    void testGetAllHearingsForCitizenCase() {
        String caseId = "1234567891234567";
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);

        Mockito.when(hearingService.getHearings(AUTH_TOKEN, caseId)).thenReturn(
            Hearings.hearingsWith().build());

        Hearings hearingForCase = caseController.getAllHearingsForCitizenCase(
            AUTH_TOKEN, S2S_TOKEN, caseId);
        assertNotNull(hearingForCase);
    }

    @Test
    void testGetAllHearingsForCaseInvalidClient() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            caseController.getAllHearingsForCitizenCase(AUTH_TOKEN, S2S_TOKEN, "test");
        });
    }

    @Test
    void testFetchIdamAmRoles() {
        String emailId = "test@email.com";
        Map<String, String> amRoles = new HashMap<>();
        amRoles.put("amRoles","case-worker");
        Mockito.when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Boolean.TRUE);
        Mockito.when(caseService.fetchIdamAmRoles(AUTH_TOKEN, emailId)).thenReturn(amRoles);

        Map<String, String> roles = caseController.fetchIdamAmRoles(
            AUTH_TOKEN, emailId);
        assertFalse(roles.isEmpty());
    }

    @Test
    void testFetchIdamAmRolesFails() {
        String emailId = "test@email.com";
        Map<String, String> amRoles = new HashMap<>();
        amRoles.put("amRoles", "case-worker");

        Mockito.when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Boolean.FALSE);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            caseController.fetchIdamAmRoles(AUTH_TOKEN, emailId);
        });

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void shouldCreateC100Case() {
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
        Mockito.when(caseService.createCase(caseData, AUTH_TOKEN)).thenReturn(caseDetails);
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        Mockito.when(launchDarklyClient.isFeatureEnabled(TASK_LIST_V3_FLAG)).thenReturn(true);
        //When
        CaseData actualCaseData = caseController.createCase(AUTH_TOKEN, S2S_TOKEN, caseData);

        //Then
        assertThat(actualCaseData).isEqualTo(caseData);
    }

}
