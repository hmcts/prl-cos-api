package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.UiCitizenCaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.TASK_LIST_V3_FLAG;

@ExtendWith(MockitoExtension.class)
public class CaseControllerTest {

    @InjectMocks
    private CaseController caseController;

    @Mock
    private CaseService caseService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    private HearingService hearingService;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    private CaseData caseData;

    private final String caseId = "1234567891234567";

    public static final String authToken = "Bearer TestAuthToken";
    public static final String servAuthToken = "Bearer TestServToken";

    @BeforeEach
    public void setUp() {

        objectMapper.registerModule(new JavaTimeModule());

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();
    }

    @Test
    public void testGetCase() {

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
        UiCitizenCaseData caseData1 = caseController.getCase(caseId, authToken, servAuthToken);

        assertEquals(caseData.getApplicantCaseName(), caseData1.getCaseData().getApplicantCaseName());
        assertEquals(true, authorisationService.authoriseUser(authToken));
        assertEquals(true, authorisationService.authoriseService(servAuthToken));
        assertEquals("servAuthToken", authTokenGenerator.generate());

        verify(authorisationService).isAuthorized(authToken, servAuthToken);
    }

    @Test
    public void testGetCaseInvalidClient() {

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            caseController.getCase(caseId, authToken, servAuthToken);
        });

        assertEquals("Invalid Client", exception.getMessage());

        verify(authorisationService).isAuthorized(authToken, servAuthToken);
    }

    @Test
    public void testGetCaseWithHearing() {

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(true);
        when(caseService.getCaseWithHearing(authToken, caseId, "test")).thenReturn(CaseDataWithHearingResponse.builder().build());

        assertNotNull(caseController.retrieveCaseWithHearing(caseId, "test", authToken, servAuthToken));
    }

    @Test
    public void testGetCaseWithHearingInvalidClient() {

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            caseController.retrieveCaseWithHearing(caseId, "test", authToken, servAuthToken);
        });

        verify(authorisationService).isAuthorized(authToken, servAuthToken);

        assertEquals("Invalid Client", exception.getMessage());
        assertFalse(authorisationService.isAuthorized(authToken, servAuthToken));

    }

    @Test
    public void testUpdateCaseInvalidClient() throws JsonProcessingException {

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            caseController.updateCase(caseData, caseId, "test", authToken, servAuthToken, "test");
        });

        verify(authorisationService).isAuthorized(authToken, servAuthToken);
        assertEquals("Invalid Client", exception.getMessage());
        assertFalse(authorisationService.isAuthorized(authToken, servAuthToken));
    }

    @Test()
    public void testRetrieveCaseInvalidClient() {

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            caseController.retrieveCases(caseId, caseId, authToken, servAuthToken);
        });

        verify(authorisationService).isAuthorized(authToken, servAuthToken);

        assertEquals("Invalid Client", exception.getMessage());
        assertFalse(authorisationService.isAuthorized(authToken, servAuthToken));
    }

    @Test()
    public void testRetrieveCitizenCaseInvalidClient() {

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            caseController.retrieveCitizenCases(authToken, servAuthToken);
        });

        verify(authorisationService).isAuthorized(authToken, servAuthToken);

        assertEquals("Invalid Client", exception.getMessage());
        assertFalse(authorisationService.isAuthorized(authToken, servAuthToken));
    }

    @Test
    public void shouldCreateCase() {

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .noOfDaysRemainingToSubmitCase(PrlAppsConstants.CASE_SUBMISSION_THRESHOLD)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseService.createCase(caseData, authToken)).thenReturn(caseDetails);
        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(true);

        CaseData actualCaseData = caseController.createCase(authToken, servAuthToken, caseData);

        verify(authorisationService).isAuthorized(authToken, servAuthToken);
        assertThat(actualCaseData).isEqualTo(caseData);
    }

    @Test()
    public void testCreateCaseInvalidClient() {

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            caseController.createCase(authToken, servAuthToken, caseData);
        });

        verify(authorisationService).isAuthorized(authToken, servAuthToken);

        assertEquals("Invalid Client", exception.getMessage());
        assertFalse(authorisationService.isAuthorized(authToken, servAuthToken));
    }

    @Test
    public void testGetAllHearingsForCitizenCase() throws IOException {

        CaseHearing caseHearing = new CaseHearing();

        caseHearing.setHearingID(1243L);
        caseHearing.setHearingDaySchedule(new ArrayList<>());
        caseHearing.setHearingGroupRequestId("test");
        caseHearing.setHearingIsLinkedFlag(true);
        caseHearing.setHearingType("test");
        caseHearing.setHearingListingStatus("test");
        caseHearing.setHearingRequestDateTime(LocalDateTime.now());
        caseHearing.setHmcStatus("test");
        caseHearing.setLastResponseReceivedDateTime(LocalDateTime.now());
        caseHearing.setHearingRequestDateTime(LocalDateTime.now());

        List<CaseHearing> caseHearingsList = new ArrayList<>();

        caseHearingsList.add(caseHearing);

        Hearings hearings = new Hearings();
        hearings.setCaseHearings(caseHearingsList);

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(true);
        when(caseController.getAllHearingsForCitizenCase(authToken, servAuthToken, caseId)).thenReturn(hearings);
        when(hearingService.getHearings(authToken, caseId)).thenReturn(hearings);

        Hearings hearingForCase = caseController.getAllHearingsForCitizenCase(
            authToken, servAuthToken, caseId);

        assertThat(hearingForCase.getCaseHearings().size()).isEqualTo(1);
    }

    @Test()
    public void testGetAllHearingsForCaseInvalidClient() {

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            caseController.getAllHearingsForCitizenCase(authToken, servAuthToken, "test");
        });

        verify(authorisationService).isAuthorized(authToken, servAuthToken);

        assertEquals("Invalid Client", exception.getMessage());
        assertFalse(authorisationService.isAuthorized(authToken, servAuthToken));
    }

    @Test
    public void testFetchIdamAmRoles() throws IOException {
        String emailId = "test@email.com";
        Map<String, String> amRoles = new HashMap<>();
        amRoles.put("amRoles","case-worker");
        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        when(caseService.fetchIdamAmRoles(authToken, emailId)).thenReturn(amRoles);

        Map<String, String> roles = caseController.fetchIdamAmRoles(
            authToken, emailId);
        assertFalse(roles.isEmpty());
    }

    @Test
    public void testFetchIdamAmRolesFails() throws IOException {

        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            caseController.fetchIdamAmRoles(authToken, "test@email.com");
        });

        assertEquals("Invalid Client", exception.getMessage());
        assertFalse(authorisationService.isAuthorized(authToken, servAuthToken));
        verify(authorisationService).authoriseUser(authToken);
    }

    @Test
    public void shouldCreateC100Case() {

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
        when(caseService.createCase(caseData, authToken)).thenReturn(caseDetails);
        when(authorisationService.isAuthorized(authToken, servAuthToken)).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn(servAuthToken);
        when(launchDarklyClient.isFeatureEnabled(TASK_LIST_V3_FLAG)).thenReturn(true);

        CaseData actualCaseData = caseController.createCase(authToken, servAuthToken, caseData);

        assertThat(actualCaseData).isEqualTo(caseData);
        assertEquals(servAuthToken, authTokenGenerator.generate());
        verify(authTokenGenerator).generate();
    }
}
