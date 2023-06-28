package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.controllers.testingsupport.TestingSupportController;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.TestingSupportService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
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
}
