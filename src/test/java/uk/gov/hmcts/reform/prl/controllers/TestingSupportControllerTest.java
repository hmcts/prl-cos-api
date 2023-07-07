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
import uk.gov.hmcts.reform.prl.services.TestingSupportService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
    String auth = "authorisation";
    String s2sAuth = "s2sAuth";

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
    }

    @Test
    public void testAboutToSubmitCaseCreation() throws Exception {
        testingSupportController.aboutToSubmitCaseCreation(auth, callbackRequest);
        verify(testingSupportService, times(1)).initiateCaseCreation(Mockito.anyString(), Mockito.any(CallbackRequest.class));
    }

    @Test
    public void testSubmittedCaseCreation() {
        testingSupportController.submittedCaseCreation(auth, callbackRequest);
        verify(testingSupportService, times(1)).submittedCaseCreation(Mockito.any(CallbackRequest.class), Mockito.anyString());
    }

    @Test
    public void testConfirmDummyPayment() {
        testingSupportController.confirmDummyPayment(auth, callbackRequest);
        verify(testingSupportService, times(1)).confirmDummyPayment(Mockito.any(CallbackRequest.class), Mockito.anyString());
    }

    @Test
    public void testCreateDummyCitizenCase() throws Exception {
        testingSupportController.createDummyCitizenCase(auth, s2sAuth);
        verify(testingSupportService, times(1)).createDummyLiPC100Case(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testFillRespondentTaskList() throws Exception {
        testingSupportController.fillRespondentTaskList(auth, callbackRequest);
        verify(testingSupportService, times(1)).initiateRespondentResponseCreation(Mockito.anyString(), Mockito.any(
            CallbackRequest.class));
    }

    @Test
    public void testSubmittedRespondentTaskList() {
        testingSupportController.submittedRespondentTaskList(auth, callbackRequest);
        verify(testingSupportService, times(1)).respondentTaskListRequestSubmitted(Mockito.any(CallbackRequest.class));
    }
}
