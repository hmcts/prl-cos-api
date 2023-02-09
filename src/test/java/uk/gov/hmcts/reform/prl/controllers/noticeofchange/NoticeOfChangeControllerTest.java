package uk.gov.hmcts.reform.prl.controllers.noticeofchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class NoticeOfChangeControllerTest {

    @InjectMocks
    private NoticeOfChangeController noticeOfChangeController;
    @Mock
    private ObjectMapper objectMapper;
    private CaseDetails caseDetails;
    private Map<String,Object> caseData;
    private AboutToStartOrSubmitCallbackResponse response;

    public static final String authToken = "test token";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        caseData = new HashMap<>();
        caseDetails = CaseDetails.builder().data(caseData).state(State.GATE_KEEPING.getValue())
            .id(123488888L).createdDate(LocalDateTime.now()).lastModified(LocalDateTime.now()).build();
    }

    @Test
    public void testAboutToSubmitNoCRequest() {
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        response = noticeOfChangeController.aboutToSubmitNoCRequest(authToken,callbackRequest);
        assertNotNull(response);
    }

    @Test
    public void testSubmittedNoCRequest() {
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        response = noticeOfChangeController.submittedNoCRequest(callbackRequest);
        assertNotNull(response);
    }
}
