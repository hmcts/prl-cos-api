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
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class NoticeOfChangeControllerTest {

    @InjectMocks
    NoticeOfChangeController noticeOfChangeController;

    @Mock
    NoticeOfChangePartiesService noticeOfChangePartiesService;

    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    Map<String, Object> caseDataMap;
    CaseDetails caseDetails;
    CaseData caseData;
    CallbackRequest callbackRequest;

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
    public void testAboutToSubmitNoCRequest() throws Exception {
        noticeOfChangeController.aboutToSubmitNoCRequest(authToken, s2sToken, callbackRequest);
        verify(noticeOfChangePartiesService, times(1)).applyDecision(Mockito.any(CallbackRequest.class), Mockito.anyString());
    }

    @Test
    public void testSubmittedNoCRequest() throws Exception {
        noticeOfChangeController.submittedNoCRequest(authToken, s2sToken, callbackRequest);
        verify(noticeOfChangePartiesService, times(1)).nocRequestSubmitted(Mockito.any(CallbackRequest.class), Mockito.anyString());
    }
}
