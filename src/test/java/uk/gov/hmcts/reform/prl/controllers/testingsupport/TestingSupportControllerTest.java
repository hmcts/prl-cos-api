package uk.gov.hmcts.reform.prl.controllers.testingsupport;

import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.controllers.testingsupport.TestingSupportController;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.TestingSupportService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestingSupportControllerTest {

    @InjectMocks
    TestingSupportController testingSupportController;

    @Mock
    TestingSupportService testingSupportService;

    Map<String, Object> caseDataMap;
    CaseDetails caseDetails;
    CaseData caseData;
    CallbackRequest callbackRequest;
    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @Before
    public void setup() {
        caseDataMap = new HashMap<>();
        caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
    }

    @Test
    public void testAboutToSubmitCaseCreation() throws Exception {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        testingSupportController.aboutToSubmitCaseCreation(authToken, s2sToken, callbackRequest);
        verify(testingSupportService, times(1)).initiateCaseCreation(Mockito.anyString(), Mockito.any(CallbackRequest.class));
    }

    @Test
    public void testSubmittedCaseCreation() {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        testingSupportController.submittedCaseCreation(authToken, s2sToken, callbackRequest);
        verify(testingSupportService, times(1)).submittedCaseCreation(Mockito.any(CallbackRequest.class), Mockito.anyString());
    }

    @Test
    public void testConfirmDummyPayment() {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        testingSupportController.confirmDummyPayment(authToken, s2sToken, callbackRequest);
        verify(testingSupportService, times(1)).confirmDummyPayment(Mockito.any(CallbackRequest.class), Mockito.anyString());
    }

    @Test
    public void testCreateDummyCitizenCase() throws Exception {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        testingSupportController.createDummyCitizenCase(authToken, s2sToken);
        verify(testingSupportService, times(1)).createDummyLiPC100Case(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testFillRespondentTaskList() throws Exception {
        testingSupportController.fillRespondentTaskList(authToken, s2sToken, callbackRequest);
        verify(testingSupportService, times(1)).initiateRespondentResponseCreation(Mockito.anyString(), Mockito.any(
            CallbackRequest.class));
    }

    @Test
    public void testSubmittedRespondentTaskList() {
        testingSupportController.submittedRespondentTaskList(authToken, s2sToken, callbackRequest);
        verify(testingSupportService, times(1)).respondentTaskListRequestSubmitted(Mockito.any(CallbackRequest.class));
    }

    @Test
    public void testExeptionForaboutToSubmitCaseCreation() {
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            testingSupportController.aboutToSubmitCaseCreation(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExeptionForfillRespondentTaskList() {
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            testingSupportController.fillRespondentTaskList(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExeptionForSubmittedCaseCreation() {
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            testingSupportController.submittedCaseCreation(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExeptionForconfirmDummyPayment() {
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            testingSupportController.confirmDummyPayment(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExeptionForCreateDummyCitizenCase() {
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            testingSupportController.createDummyCitizenCase(authToken, s2sToken);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExeptionForSubmittedRespondentTaskList() {
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            testingSupportController.submittedRespondentTaskList(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testAboutToSubmitCaseCreationCourtNav() throws Exception {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        testingSupportController.aboutToSubmitCaseCreationCourtNav(authToken, s2sToken, callbackRequest);
        verify(testingSupportService, times(1)).initiateCaseCreationForCourtNav(Mockito.anyString(), Mockito.any(CallbackRequest.class));
    }

    @Test
    public void testExeptionForaboutToSubmitCaseCreationCourtNav() {
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            testingSupportController.aboutToSubmitCaseCreationCourtNav(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testConfirmDummyAwPPayment() {
        testingSupportController.confirmDummyAwPPayment(authToken, callbackRequest);
        verify(testingSupportService, times(1)).confirmDummyAwPPayment(Mockito.any(CallbackRequest.class), Mockito.anyString());
    }


    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
