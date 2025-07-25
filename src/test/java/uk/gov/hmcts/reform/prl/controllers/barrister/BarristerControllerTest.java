package uk.gov.hmcts.reform.prl.controllers.barrister;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.barrister.BarristerAllocationService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BarristerControllerTest {
    @InjectMocks
    private BarristerController barristerController;

    @Mock
    private BarristerAllocationService barristerAllocationService;
    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private ObjectMapper objectMapper;

    private static final String AUTH_TOKEN = "auth-token";
    private static final String SERVICE_TOKEN = "service-token";

    @Test
    public void shouldHandleMidEvent() {
        Map caseData = new HashMap<>();
        caseData.put("id", 12345L);
        caseData.put("caseTypeOfApplication", "C100");

        CaseData caseData1 = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData)
                             .build())
            .build();

        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData1);
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);

        AllocatedBarrister allocatedBarrister = AllocatedBarrister.builder().build();
        when(barristerAllocationService.getAllocatedBarrister(any())).thenReturn(allocatedBarrister);
        AboutToStartOrSubmitCallbackResponse callbackResponse = barristerController
            .handleMidEvent(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest);

        assertEquals(allocatedBarrister, callbackResponse.getData().get("allocatedBarrister"));

        verify(barristerAllocationService, times(1)).getAllocatedBarrister(any());
    }

    @Test
    public void shouldNotHandleMidEventWhenNotAuthorised() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .build())
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);

        assertThrows(
            RuntimeException.class,
            () -> barristerController
                .handleMidEvent(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest));
    }
}
