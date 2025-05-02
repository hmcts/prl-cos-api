package uk.gov.hmcts.reform.prl.controllers.citizen;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    private final Long caseIdNumber = Long.valueOf(caseId);
    private final String testValue = "testValue";
    private final String invalidClient = "Invalid Client";
    private final String testEmail = "test@email.com";
    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String SERVICE_AUTH_TOKEN = "Bearer TestServToken";

    @BeforeEach
    void setUp() {

        objectMapper.registerModule(new JavaTimeModule());

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName(testValue)
            .createdDate(LocalDateTime.now().minusDays(10))
            .build();
    }

    @Test
    void testGetCase() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN)).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper().registerModule(new JavaTimeModule()));
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseService.getCase(AUTH_TOKEN, caseId)).thenReturn(caseDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(true);
        when(authorisationService.authoriseService(SERVICE_AUTH_TOKEN)).thenReturn(true);
        UiCitizenCaseData caseData1 = caseController.getCase(caseId, AUTH_TOKEN, SERVICE_AUTH_TOKEN);

        assertEquals(caseData.getApplicantCaseName(), caseData1.getCaseData().getApplicantCaseName());
        assertEquals(true, authorisationService.authoriseUser(AUTH_TOKEN));
        assertEquals(true, authorisationService.authoriseService(SERVICE_AUTH_TOKEN));
        assertEquals(SERVICE_AUTH_TOKEN, authTokenGenerator.generate());

        verify(authorisationService).isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN);
    }

    @Test
    void testGetCaseInvalidClient() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            caseController.getCase(caseId, AUTH_TOKEN, SERVICE_AUTH_TOKEN)
        );

        assertEquals(invalidClient, exception.getMessage());

        verify(authorisationService).isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN);
    }

    @Test
    void testGetCaseWithHearing() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN)).thenReturn(true);
        when(caseService.getCaseWithHearing(AUTH_TOKEN, caseId, testValue)).thenReturn(CaseDataWithHearingResponse.builder().build());

        assertNotNull(caseController.retrieveCaseWithHearing(caseId, testValue, AUTH_TOKEN, SERVICE_AUTH_TOKEN));
    }

    @Test
    void testGetCaseWithHearingInvalidClient() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            caseController.retrieveCaseWithHearing(caseId, testValue, AUTH_TOKEN, SERVICE_AUTH_TOKEN)
        );

        verify(authorisationService).isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN);

        assertEquals(invalidClient, exception.getMessage());
        assertFalse(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN));

    }

    @Test
    void testUpdateCaseInvalidClient() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            caseController.updateCase(caseData, caseId, testValue, AUTH_TOKEN, SERVICE_AUTH_TOKEN, testValue)
        );

        verify(authorisationService).isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN);
        assertEquals(invalidClient, exception.getMessage());
        assertFalse(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN));
    }

    @Test()
    void testRetrieveCaseInvalidClient() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            caseController.retrieveCases(caseId, caseId, AUTH_TOKEN, SERVICE_AUTH_TOKEN)
        );

        verify(authorisationService).isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN);

        assertEquals(invalidClient, exception.getMessage());
        assertFalse(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN));
    }

    @Test()
    void testRetrieveCitizenCaseInvalidClient() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            caseController.retrieveCitizenCases(AUTH_TOKEN, SERVICE_AUTH_TOKEN)
        );

        verify(authorisationService).isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN);

        assertEquals(invalidClient, exception.getMessage());
        assertFalse(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN));
    }

    @Test
    void shouldCreateCase() {

        caseData = CaseData.builder()
            .id(caseIdNumber)
            .applicantCaseName(testValue)
            .noOfDaysRemainingToSubmitCase(PrlAppsConstants.CASE_SUBMISSION_THRESHOLD)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            caseIdNumber).data(stringObjectMap).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseService.createCase(caseData, AUTH_TOKEN)).thenReturn(caseDetails);
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN)).thenReturn(true);

        CaseData actualCaseData = caseController.createCase(AUTH_TOKEN, SERVICE_AUTH_TOKEN, caseData);

        verify(authorisationService).isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN);
        assertThat(actualCaseData).isEqualTo(caseData);
    }

    @Test()
    void testCreateCaseInvalidClient() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            caseController.createCase(AUTH_TOKEN, SERVICE_AUTH_TOKEN, caseData)
        );

        verify(authorisationService).isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN);

        assertEquals(invalidClient, exception.getMessage());
        assertFalse(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN));
    }

    @Test
    void testGetAllHearingsForCitizenCase() {

        CaseHearing caseHearing = new CaseHearing();

        caseHearing.setHearingID(caseIdNumber);
        caseHearing.setHearingDaySchedule(new ArrayList<>());
        caseHearing.setHearingGroupRequestId(testValue);
        caseHearing.setHearingIsLinkedFlag(true);
        caseHearing.setHearingType(testValue);
        caseHearing.setHearingListingStatus(testValue);
        caseHearing.setHearingRequestDateTime(LocalDateTime.now());
        caseHearing.setHmcStatus(testValue);
        caseHearing.setLastResponseReceivedDateTime(LocalDateTime.now());
        caseHearing.setHearingRequestDateTime(LocalDateTime.now());

        List<CaseHearing> caseHearingsList = new ArrayList<>();

        caseHearingsList.add(caseHearing);

        Hearings hearings = new Hearings();
        hearings.setCaseHearings(caseHearingsList);

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN)).thenReturn(true);
        when(caseController.getAllHearingsForCitizenCase(AUTH_TOKEN, SERVICE_AUTH_TOKEN, caseId)).thenReturn(hearings);
        when(hearingService.getHearings(AUTH_TOKEN, caseId)).thenReturn(hearings);

        Hearings hearingForCase = caseController.getAllHearingsForCitizenCase(
            AUTH_TOKEN, SERVICE_AUTH_TOKEN, caseId);

        assertThat(hearingForCase.getCaseHearings()).hasSize(1);
    }

    @Test()
    void testGetAllHearingsForCaseInvalidClient() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            caseController.getAllHearingsForCitizenCase(AUTH_TOKEN, SERVICE_AUTH_TOKEN, testValue)
        );

        verify(authorisationService).isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN);

        assertEquals(invalidClient, exception.getMessage());
        assertFalse(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN));
    }

    @Test
    void testFetchIdamAmRoles() {
        String emailId = testEmail;
        Map<String, String> amRoles = new HashMap<>();
        amRoles.put("amRoles","case-worker");
        when(authorisationService.authoriseUser(AUTH_TOKEN)).thenReturn(Boolean.TRUE);
        when(caseService.fetchIdamAmRoles(AUTH_TOKEN, emailId)).thenReturn(amRoles);

        Map<String, String> roles = caseController.fetchIdamAmRoles(
            AUTH_TOKEN, emailId);
        assertFalse(roles.isEmpty());
    }

    @Test
    void testFetchIdamAmRolesFails() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            caseController.fetchIdamAmRoles(AUTH_TOKEN, testEmail)
        );

        assertEquals(invalidClient, exception.getMessage());
        assertFalse(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN));
        verify(authorisationService).authoriseUser(AUTH_TOKEN);
    }

    @Test
    void shouldCreateC100Case() {

        caseData = CaseData.builder()
            .id(caseIdNumber)
            .applicantCaseName(testValue)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .noOfDaysRemainingToSubmitCase(PrlAppsConstants.CASE_SUBMISSION_THRESHOLD)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            caseIdNumber).data(stringObjectMap).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseService.createCase(caseData, AUTH_TOKEN)).thenReturn(caseDetails);
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH_TOKEN)).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(launchDarklyClient.isFeatureEnabled(TASK_LIST_V3_FLAG)).thenReturn(true);

        CaseData actualCaseData = caseController.createCase(AUTH_TOKEN, SERVICE_AUTH_TOKEN, caseData);

        assertThat(actualCaseData).isEqualTo(caseData);
        assertEquals(SERVICE_AUTH_TOKEN, authTokenGenerator.generate());
        verify(authTokenGenerator).generate();
    }
}
