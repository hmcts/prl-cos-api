package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TESTING_SUPPORT_LD_FLAG_ENABLED;
import static uk.gov.hmcts.reform.prl.enums.Event.TS_ADMIN_APPLICATION_NOC;
import static uk.gov.hmcts.reform.prl.enums.Event.TS_SOLICITOR_APPLICATION;

@RunWith(MockitoJUnitRunner.class)
public class TestingSupportServiceTest {

    @InjectMocks
    TestingSupportService testingSupportService;

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private EventService eventPublisher;
    @Mock
    private AllTabServiceImpl tabService;
    @Mock
    private UserService userService;
    @Mock
    private DocumentGenService dgsService;

    @Mock
    private CaseUtils caseUtils;
    @Mock
    private CaseWorkerEmailService caseWorkerEmailService;
    @Mock
    private LaunchDarklyClient launchDarklyClient;
    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private RequestUpdateCallbackService requestUpdateCallbackService;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CaseService caseService;

    Map<String, Object> caseDataMap;
    CaseDetails caseDetails;
    CaseData caseData;
    CallbackRequest callbackRequest;
    String auth = "authorisation";
    String s2sAuth = "s2sAuth";

    @Before
    public void setUp() throws Exception {
        when(launchDarklyClient.isFeatureEnabled(TESTING_SUPPORT_LD_FLAG_ENABLED)).thenReturn(true);
        when(authorisationService.authoriseUser(anyString())).thenReturn(Boolean.TRUE);
        when(authorisationService.authoriseService(anyString())).thenReturn(Boolean.TRUE);
    }

    @Test
    public void testAboutToSubmitCaseCreationWithoutDummyData() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
            .state(State.SUBMITTED_PAID)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.SUBMITTED_PAID.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId(TS_SOLICITOR_APPLICATION.getId())
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        Map<String, Object> stringObjectMap = testingSupportService.initiateCaseCreation(auth, callbackRequest);
        Assert.assertTrue(stringObjectMap.isEmpty());
    }

    @Test
    public void testAboutToSubmitSolicitorCaseCreationWithDummyC100Data() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId(TS_SOLICITOR_APPLICATION.getId())
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(caseDetails);

        Map<String, Object> stringObjectMap = testingSupportService.initiateCaseCreation(auth, callbackRequest);
        Assert.assertTrue(!stringObjectMap.isEmpty());
    }

    @Test
    public void testAboutToSubmitSolicitorCaseCreationWithDummyFl401Data() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId(TS_SOLICITOR_APPLICATION.getId())
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(caseDetails);
        Map<String, Object> stringObjectMap = testingSupportService.initiateCaseCreation(auth, callbackRequest);
        Assert.assertTrue(!stringObjectMap.isEmpty());
    }


    @Test
    public void testAboutToSubmitAdminCaseCreationWithDummyC100Data() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId(TS_ADMIN_APPLICATION_NOC.getId())
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(caseDetails);
        Map<String, Object> stringObjectMap = testingSupportService.initiateCaseCreation(auth, callbackRequest);
        Assert.assertTrue(!stringObjectMap.isEmpty());
    }

    @Test
    public void testAboutToSubmitAdminCaseCreationWithDummyFl401Data() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId(TS_ADMIN_APPLICATION_NOC.getId())
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(caseDetails);
        Map<String, Object> stringObjectMap = testingSupportService.initiateCaseCreation(auth, callbackRequest);
        Assert.assertTrue(!stringObjectMap.isEmpty());
    }

    @Test
    public void testAboutToSubmitAdminCaseCreationWithDummyFl401DataDocumentCreationError() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId(TS_ADMIN_APPLICATION_NOC.getId())
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(caseDetails);
        when(dgsService.generateDocumentsForTestingSupport(
            anyString(),
            any(CaseData.class)
        )).thenThrow(RuntimeException.class);
        Map<String, Object> stringObjectMap = testingSupportService.initiateCaseCreation(auth, callbackRequest);
        Assert.assertTrue(!stringObjectMap.isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void testAboutToSubmitAdminCaseCreationInvalidClient() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId(TS_ADMIN_APPLICATION_NOC.getId())
            .build();
        when(launchDarklyClient.isFeatureEnabled(TESTING_SUPPORT_LD_FLAG_ENABLED)).thenReturn(true);
        when(authorisationService.authoriseUser(anyString())).thenReturn(Boolean.FALSE);
        testingSupportService.initiateCaseCreation(auth, callbackRequest);
    }

    @Test
    public void testSubmittedCaseCreation() {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId(TS_ADMIN_APPLICATION_NOC.getId())
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        Map<String, Object> stringObjectMap = testingSupportService.submittedCaseCreation(callbackRequest, auth);
        Assert.assertTrue(!stringObjectMap.isEmpty());
        verify(eventPublisher, times(1)).publishEvent(Mockito.any(CaseDataChanged.class));
    }

    @Test(expected = RuntimeException.class)
    public void testSubmittedCaseCreationWithInvalidClient() {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId(TS_ADMIN_APPLICATION_NOC.getId())
            .build();
        when(launchDarklyClient.isFeatureEnabled(TESTING_SUPPORT_LD_FLAG_ENABLED)).thenReturn(true);
        when(authorisationService.authoriseUser(anyString())).thenReturn(Boolean.FALSE);
        testingSupportService.submittedCaseCreation(callbackRequest, auth);
    }

    @Test
    public void testConfirmDummyPayment() {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId(TS_ADMIN_APPLICATION_NOC.getId())
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(coreCaseDataApi.getCase(
            any(),
            any(),
            any()
        )).thenReturn(caseDetails);
        Map<String, Object> stringObjectMap = testingSupportService.confirmDummyPayment(callbackRequest, auth);
        Assert.assertTrue(!stringObjectMap.isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void testConfirmDummyPaymentWithInvalidClient() {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId(TS_ADMIN_APPLICATION_NOC.getId())
            .build();
        when(launchDarklyClient.isFeatureEnabled(TESTING_SUPPORT_LD_FLAG_ENABLED)).thenReturn(false);
        testingSupportService.confirmDummyPayment(callbackRequest, auth);
    }

    @Test
    public void testCreateDummyLiPC100Case() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId(TS_SOLICITOR_APPLICATION.getId())
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(caseDetails);
        when(caseService.createCase(Mockito.any(), Mockito.anyString())).thenReturn(caseDetails);

        CaseData updatedCaseData = testingSupportService.createDummyLiPC100Case(auth, s2sAuth);
        assertEquals(12345678L, updatedCaseData.getId());
    }

    @Test(expected = RuntimeException.class)
    public void testCreateDummyLiPC100Case_InvalidClient_LdDisabled() throws Exception {
        when(launchDarklyClient.isFeatureEnabled(TESTING_SUPPORT_LD_FLAG_ENABLED)).thenReturn(false);
        testingSupportService.createDummyLiPC100Case(auth, s2sAuth);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateDummyLiPC100Case_InvalidS2S() throws Exception {
        when(authorisationService.authoriseService(anyString())).thenReturn(false);
        testingSupportService.createDummyLiPC100Case(auth, s2sAuth);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateDummyLiPC100Case_InvalidAuthorisation() throws Exception {
        when(authorisationService.authoriseUser(anyString())).thenReturn(false);
        testingSupportService.createDummyLiPC100Case(auth, s2sAuth);
    }
}
