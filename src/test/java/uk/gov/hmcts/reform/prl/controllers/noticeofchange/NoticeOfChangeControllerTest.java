package uk.gov.hmcts.reform.prl.controllers.noticeofchange;

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
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeOfChangeControllerTest {

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
    void testAboutToSubmitNoCRequest() throws Exception {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        noticeOfChangeController.aboutToSubmitNoCRequest(authToken, s2sToken, callbackRequest);
        verify(noticeOfChangePartiesService, times(1)).applyDecision(
            Mockito.any(CallbackRequest.class),
            Mockito.anyString()
        );
    }

    @Test
    void testSubmittedNoCRequest() throws Exception {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        noticeOfChangeController.submittedNoCRequest(authToken, s2sToken, callbackRequest);
        verify(noticeOfChangePartiesService, times(1)).nocRequestSubmitted(
            Mockito.any(CallbackRequest.class));
    }

    @Test
    void testStartStopRepresentation() throws Exception {
        noticeOfChangeController.aboutToStartStopRepresentation(authToken, callbackRequest);
        verify(noticeOfChangePartiesService, times(1)).populateAboutToStartStopRepresentation(
            Mockito.anyString(),
            Mockito.any(CallbackRequest.class),
            Mockito.anyList()
        );
    }

    @Test
    void testAboutToSubmitStopRepresentation() throws Exception {
        noticeOfChangeController.aboutToSubmitStopRepresentation(authToken, callbackRequest);
        verify(noticeOfChangePartiesService, times(1)).aboutToSubmitStopRepresenting(
            Mockito.anyString(),
            Mockito.any(CallbackRequest.class));
    }

    @Test
    void testSubmittedStopRepresentation() throws Exception {
        noticeOfChangeController.submittedStopRepresentation(authToken, callbackRequest);
        verify(noticeOfChangePartiesService, times(1)).submittedStopRepresenting(
            Mockito.any(CallbackRequest.class)
        );
    }

    @Test
    void testAboutToStartAdminRemoveLegalRepresentative() throws Exception {
        noticeOfChangeController.aboutToStartAdminRemoveLegalRepresentative(authToken, callbackRequest);
        verify(noticeOfChangePartiesService, times(1))
            .populateAboutToStartAdminRemoveLegalRepresentative(
                Mockito.any(CallbackRequest.class),
                Mockito.anyList()
            );
    }

    @Test
    void testAboutToSubmitAdminRemoveLegalRepresentative() throws Exception {
        noticeOfChangeController.aboutToSubmitAdminRemoveLegalRepresentative(authToken, callbackRequest);
        verify(noticeOfChangePartiesService, times(1))
            .aboutToSubmitAdminRemoveLegalRepresentative(
                Mockito.anyString(),
                Mockito.any(CallbackRequest.class)
            );
    }

    @Test
    void testSubmittedAdminRemoveLegalRepresentative() throws Exception {
        noticeOfChangeController.submittedAdminRemoveLegalRepresentative(authToken, callbackRequest);
        verify(noticeOfChangePartiesService, times(1))
            .submittedAdminRemoveLegalRepresentative(
                Mockito.any(CallbackRequest.class)
            );
    }
}
