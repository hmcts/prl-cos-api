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
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.barrister.BarristerAllocationService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    private UserDetails userDetails;

    private static final String authToken = "Bearer TestAuthToken";

    @Test
    public void shouldReturnAllocatedBarristerDetails() {
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

        AllocatedBarrister allocatedBarrister = AllocatedBarrister.builder().build();
        when(barristerAllocationService.getAllocatedBarrister(any(), eq(userDetails), any())).thenReturn(allocatedBarrister);
        AboutToStartOrSubmitCallbackResponse callbackResponse = barristerController.handleMidEvent(
            authToken,
            callbackRequest
        );

        assertEquals(allocatedBarrister, callbackResponse.getData().get("allocatedBarrister"));

        verify(barristerAllocationService, times(1)).getAllocatedBarrister(any(), eq(userDetails), any());
    }

}
