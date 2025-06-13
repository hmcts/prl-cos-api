package uk.gov.hmcts.reform.prl.controllers.testingsupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.TestingSupportService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestingSupportControllerTest {

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

    @BeforeEach
    void setup() {
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
    void testAboutToSubmitCaseCreation() throws Exception {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        testingSupportController.aboutToSubmitCaseCreation(authToken, s2sToken, callbackRequest);
        verify(testingSupportService, times(1)).initiateCaseCreation(Mockito.anyString(), Mockito.any(CallbackRequest.class));
    }

    @Test
    void testSubmittedCaseCreation() {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        testingSupportController.submittedCaseCreation(authToken, s2sToken, callbackRequest);
        verify(testingSupportService, times(1)).submittedCaseCreation(Mockito.any(CallbackRequest.class), Mockito.anyString());
    }

    @Test
    void testConfirmDummyPayment() {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        testingSupportController.confirmDummyPayment(authToken, s2sToken, callbackRequest);
        verify(testingSupportService, times(1)).confirmDummyPayment(Mockito.any(CallbackRequest.class), Mockito.anyString());
    }

    @Test
    void testCreateDummyCitizenCase() throws Exception {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        testingSupportController.createDummyCitizenCase(authToken, s2sToken);
        verify(testingSupportService, times(1)).createDummyLiPC100Case(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void testCreateDummyCitizenCaseWithBody() throws Exception {
        String testBody = "test body";
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        testingSupportController.createDummyCitizenCaseWithBody(authToken, s2sToken, testBody);
        verify(testingSupportService, times(1)).createDummyLiPC100CaseWithBody(Mockito.anyString(), Mockito.anyString(), Mockito.matches(testBody));
    }

    @Test
    void testFillRespondentTaskList() throws Exception {
        testingSupportController.fillRespondentTaskList(authToken, s2sToken, callbackRequest);
        verify(testingSupportService, times(1)).initiateRespondentResponseCreation(Mockito.anyString(), Mockito.any(
            CallbackRequest.class));
    }

    @Test
    void testSubmittedRespondentTaskList() {
        testingSupportController.submittedRespondentTaskList(authToken, s2sToken, callbackRequest);
        verify(testingSupportService, times(1)).respondentTaskListRequestSubmitted(Mockito.any(CallbackRequest.class));
    }

    @Test
    void testExceptionForAboutToSubmitCaseCreation() {
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            testingSupportController.aboutToSubmitCaseCreation(authToken, s2sToken, callbackRequest);
        });

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForFillRespondentTaskList() {
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            testingSupportController.fillRespondentTaskList(authToken, s2sToken, callbackRequest);
        });

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForSubmittedCaseCreation() {
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            testingSupportController.submittedCaseCreation(authToken, s2sToken, callbackRequest);
        });

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForConfirmDummyPayment() {
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            testingSupportController.confirmDummyPayment(authToken, s2sToken, callbackRequest);
        });

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForCreateDummyCitizenCase() {
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            testingSupportController.createDummyCitizenCase(authToken, s2sToken);
        });

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForCreateDummyCitizenCaseWithBody() {
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            testingSupportController.createDummyCitizenCaseWithBody(authToken, s2sToken, "test body");
        });

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExeptionForSubmittedRespondentTaskList() {
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            testingSupportController.submittedRespondentTaskList(authToken, s2sToken, callbackRequest);
        });

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testAboutToSubmitCaseCreationCourtNav() throws Exception {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        testingSupportController.aboutToSubmitCaseCreationCourtNav(authToken, s2sToken, callbackRequest);
        verify(testingSupportService, times(1)).initiateCaseCreationForCourtNav(Mockito.anyString(), Mockito.any(CallbackRequest.class));
    }

    @Test
    void testExceptionForAboutToSubmitCaseCreationCourtNav() {
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            testingSupportController.aboutToSubmitCaseCreationCourtNav(authToken, s2sToken, callbackRequest);
        });

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testConfirmDummyAwPPayment() {
        testingSupportController.confirmDummyAwPPayment(authToken, callbackRequest);
        verify(testingSupportService, times(1)).confirmDummyAwPPayment(Mockito.any(CallbackRequest.class), Mockito.anyString());
    }
}
