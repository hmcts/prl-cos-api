package uk.gov.hmcts.reform.prl.controllers.barrister;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.barrister.BarristerAddService;
import uk.gov.hmcts.reform.prl.services.barrister.BarristerRemoveService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BarristerControllerTest {
    @InjectMocks
    private BarristerController barristerController;

    @Mock
    private BarristerAddService barristerAddService;

    @Mock
    private BarristerRemoveService barristerRemoveService;

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
        when(barristerAddService.getAllocatedBarrister(caseData1, AUTH_TOKEN)).thenReturn(allocatedBarrister);
        AboutToStartOrSubmitCallbackResponse callbackResponse = barristerController
            .handleMidEvent(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest);

        assertEquals(allocatedBarrister, callbackResponse.getData().get("allocatedBarrister"));

        verify(barristerAddService, times(1)).getAllocatedBarrister(caseData1, AUTH_TOKEN);
    }

    @Test
    public void handleRemoveAboutToStart() {
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

        DynamicListElement dynamicListElement = DynamicListElement.builder().code("12345:").label("test")
            .build();

        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData1);
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);

        AllocatedBarrister allocatedBarrister = AllocatedBarrister.builder()
            .partyList(DynamicList.builder().listItems(Lists.newArrayList(dynamicListElement)).build()).build();
        when(barristerRemoveService.getBarristerListToRemove(caseData1, AUTH_TOKEN)).thenReturn(allocatedBarrister);
        AboutToStartOrSubmitCallbackResponse callbackResponse = barristerController
            .handleRemoveAboutToStart(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest);

        assertEquals(allocatedBarrister, callbackResponse.getData().get("allocatedBarrister"));

        verify(barristerRemoveService, times(1)).getBarristerListToRemove(caseData1, AUTH_TOKEN);
    }

    @Test
    public void handleRemoveAboutToStartWhenNoBarristerList() {
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

        AllocatedBarrister allocatedBarrister = AllocatedBarrister.builder()
            .partyList(DynamicList.builder()
                           .listItems(Lists.newArrayList()).build()).build();
        when(barristerRemoveService.getBarristerListToRemove(caseData1, AUTH_TOKEN)).thenReturn(allocatedBarrister);
        AboutToStartOrSubmitCallbackResponse callbackResponse = barristerController
            .handleRemoveAboutToStart(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest);

        assertEquals("No barrister currently assigned to any party", callbackResponse.getErrors().get(0));
        verify(barristerRemoveService, times(1)).getBarristerListToRemove(caseData1, AUTH_TOKEN);
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

    @Test
    public void shouldNotHandleRemoveAboutToStartWhenNotAuthorised() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .build())
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);

        assertThrows(
            RuntimeException.class,
            () -> barristerController
                .handleRemoveAboutToStart(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest));
    }
}
