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
import uk.gov.hmcts.reform.prl.exception.InvalidClientException;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.barrister.BarristerAddService;
import uk.gov.hmcts.reform.prl.services.barrister.BarristerRemoveService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

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
    public void shouldHandleAboutToStartEvent() {
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
                    .listItems(List.of(DynamicListElement.builder().code("code").label("label").build()))
                    .build())
            .build();
        when(barristerAddService.getAllocatedBarrister(caseData1, AUTH_TOKEN)).thenReturn(allocatedBarrister);
        AboutToStartOrSubmitCallbackResponse callbackResponse = barristerController
            .handleAddAboutToStartEvent(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest);

        assertEquals(allocatedBarrister, callbackResponse.getData().get("allocatedBarrister"));

        verify(barristerAddService, times(1)).getAllocatedBarrister(caseData1, AUTH_TOKEN);
    }

    @Test
    public void shouldReturnErrorsOnAboutToStartEvent() {
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
                           .listItems(new ArrayList<DynamicListElement>())
                           .build())
            .build();
        when(barristerAddService.getAllocatedBarrister(caseData1, AUTH_TOKEN)).thenReturn(allocatedBarrister);
        AboutToStartOrSubmitCallbackResponse callbackResponse = barristerController
            .handleAddAboutToStartEvent(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest);

        assertEquals(1, callbackResponse.getErrors().size());
        assertEquals("There are no solicitors currently assigned to any party on this case", callbackResponse
            .getErrors().get(0));

        verify(barristerAddService, times(1)).getAllocatedBarrister(caseData1, AUTH_TOKEN);
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
    public void shouldNotHandleAddAboutToStartEventWhenNotAuthorised() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .build())
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);

        assertThrows(
            RuntimeException.class,
            () -> barristerController
                .handleAddAboutToStartEvent(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest));
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

    @Test
    public void handleAddSubmittedSuccess() {
        Map caseDataMap = new HashMap<>();
        caseDataMap.put("id", 12345L);
        caseDataMap.put("caseTypeOfApplication", "C100");

        AllocatedBarrister allocatedBarrister = AllocatedBarrister.builder()
            .partyList(DynamicList.builder()
                           .listItems(Lists.newArrayList()).build()).build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .allocatedBarrister(allocatedBarrister)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseDataMap)
                             .build())
            .build();

        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);

        barristerController.handleAddSubmitted(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest);

        verify(barristerAddService).notifyBarrister(caseData);
    }

    @Test
    public void shouldNothandleAddSubmittedWhenNotAuthorised() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .build())
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);

        assertThrows(
            RuntimeException.class,
            () -> barristerController
                .handleAddSubmitted(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest));
    }

    @Test
    public void testSuccessSubmittedRemoveBarrister() {
        when(authorisationService.isAuthorized(any(), any()))
            .thenReturn(true);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("id", 12345L);
        caseDataMap.put("caseTypeOfApplication", "C100");

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .allocatedBarrister(AllocatedBarrister.builder()
                                    .build())
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseDataMap)
                             .build())
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class))
            .thenReturn(caseData);

        barristerController.handleRemoveSubmitted(AUTH_TOKEN,
                                                  SERVICE_TOKEN,
                                                  callbackRequest);
        verify(barristerRemoveService)
            .notifyBarrister(isA(CaseData.class));
    }


    @Test
    public void testInvalidClientExceptionForSubmittedRemoveBarrister() {
        when(authorisationService.isAuthorized(any(), any()))
            .thenReturn(false);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .build())
            .build();
        assertThatThrownBy(() -> barristerController.handleRemoveSubmitted(AUTH_TOKEN,
                                                                           SERVICE_TOKEN,
                                                                           callbackRequest))
            .isInstanceOf(InvalidClientException.class)
            .hasMessageContaining(INVALID_CLIENT);
    }
}
