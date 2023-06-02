package uk.gov.hmcts.reform.prl.controllers.noticeofchange;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class NoticeOfChangeControllerTest {

    @InjectMocks
    NoticeOfChangeController noticeOfChangeController;

    @Mock
    NoticeOfChangePartiesService noticeOfChangePartiesService;

    Map<String, Object> caseDataMap;
    CaseDetails caseDetails;
    CaseData caseData;
    CallbackRequest callbackRequest;
    String auth = "authorisation";

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
    public void testAboutToSubmitNoCRequest() throws Exception {
        noticeOfChangeController.aboutToSubmitNoCRequest(auth, callbackRequest);
        verify(noticeOfChangePartiesService, times(1)).applyDecision(
            Mockito.any(CallbackRequest.class),
            Mockito.anyString()
        );
    }

    @Test
    public void testSubmittedNoCRequest() throws Exception {
        noticeOfChangeController.submittedNoCRequest(auth, callbackRequest);
        verify(noticeOfChangePartiesService, times(1)).nocRequestSubmitted(
            Mockito.any(CallbackRequest.class));
    }

    @Test
    public void testStartStopRepresentation() throws Exception {
        noticeOfChangeController.aboutToStartStopRepresentation(auth, callbackRequest);
        verify(noticeOfChangePartiesService, times(1)).populateAboutToStartStopRepresentation(
            Mockito.anyString(),
            Mockito.any(CallbackRequest.class),
            Mockito.anyList()
        );
    }

    @Test
    public void testAboutToSubmitStopRepresentation() throws Exception {
        noticeOfChangeController.aboutToSubmitStopRepresentation(auth, callbackRequest);
        verify(noticeOfChangePartiesService, times(1)).aboutToSubmitStopRepresenting(
            Mockito.anyString(),
            Mockito.any(CallbackRequest.class));
    }

    @Test
    public void testSubmittedStopRepresentation() throws Exception {
        noticeOfChangeController.submittedStopRepresentation(auth, callbackRequest);
        verify(noticeOfChangePartiesService, times(1)).submittedStopRepresenting(
            Mockito.any(CallbackRequest.class)
        );
    }

    @Test
    public void testAboutToStartAdminRemoveLegalRepresentative() throws Exception {
        noticeOfChangeController.aboutToStartAdminRemoveLegalRepresentative(auth, callbackRequest);
        verify(noticeOfChangePartiesService, times(1))
            .populateAboutToStartAdminRemoveLegalRepresentative(
                Mockito.any(CallbackRequest.class),
                Mockito.anyList()
            );
    }

    @Test
    public void testAboutToSubmitAdminRemoveLegalRepresentative() throws Exception {
        noticeOfChangeController.aboutToSubmitAdminRemoveLegalRepresentative(auth, callbackRequest);
        verify(noticeOfChangePartiesService, times(1))
            .aboutToSubmitAdminRemoveLegalRepresentative(
                Mockito.anyString(),
                Mockito.any(CallbackRequest.class)
            );
    }

    @Test
    public void testSubmittedAdminRemoveLegalRepresentative() throws Exception {
        noticeOfChangeController.submittedAdminRemoveLegalRepresentative(auth, callbackRequest);
        verify(noticeOfChangePartiesService, times(1))
            .submittedAdminRemoveLegalRepresentative(
                Mockito.any(CallbackRequest.class)
            );
    }
}
