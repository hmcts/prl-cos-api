package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.controllers.editreturnedorder.EditReturnedOrderController;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.services.EditReturnedOrderService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@ExtendWith(MockitoExtension.class)
@PropertySource(value = "classpath:application.yaml")
public class EditReturnedOrderControllerTest {

    @Mock
    private  ObjectMapper objectMapper;
    @Mock
    private  DraftAnOrderService draftAnOrderService;

    @Mock
    private EditReturnedOrderService editReturnedOrderService;

    @Mock
    AllTabServiceImpl allTabsService;

    @InjectMocks
    private EditReturnedOrderController editReturnedOrderController;

    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @BeforeEach
    void setUp() {
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any(),
                                                              Mockito.any(), Mockito.anyString(),
                                                              Mockito.anyString())).thenReturn(DraftOrder.builder().build());
    }

    @Test
    void testGenerateReturnedOrdersDropdown() {
        Map<String, Object> caseDataMap = new HashMap<>();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        caseDataMap.put("rejectedOrdersDynamicList", DynamicList.builder().build());
        when(editReturnedOrderService.handleAboutToStartCallback(Mockito.anyString(), Mockito.any()))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder().data(caseDataMap).build());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(caseDataMap)
                             .build())
            .build();
        AboutToStartOrSubmitCallbackResponse response = editReturnedOrderController
            .handleAboutToStart(authToken,s2sToken,callbackRequest);
        assertTrue(response.getData().containsKey("rejectedOrdersDynamicList"));
    }

    @Test
    void testInvaliddClientOnHandleAboutToStart() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData, Event.EDIT_AND_APPROVE_ORDER.getId(), authToken)).thenReturn(caseDataMap);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(caseDataMap)
                             .build())
            .build();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            editReturnedOrderController.handleAboutToStart(authToken, s2sToken, callbackRequest);
        });
        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testPopulateInstructions() {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(editReturnedOrderService.populateInstructionsAndFieldsForLegalRep(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder().errors(List.of("error1")).build());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .build())
            .build();
        AboutToStartOrSubmitCallbackResponse response = editReturnedOrderController
            .populateInstructionsToSolicitor(authToken,s2sToken,PrlAppsConstants.ENGLISH,callbackRequest);
        assertFalse(response.getErrors().isEmpty());
    }

    @Test
    void testInvaliddClientOnPopulateInstructionsCallBack() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData, Event.EDIT_AND_APPROVE_ORDER.getId(), authToken)).thenReturn(caseDataMap);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(caseDataMap)
                             .build())
            .build();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            editReturnedOrderController.populateInstructionsToSolicitor(authToken, s2sToken, PrlAppsConstants.ENGLISH, callbackRequest);
        });
        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testInvaliddClientOnSubmittedCallBack() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData, Event.EDIT_AND_APPROVE_ORDER.getId(), authToken)).thenReturn(caseDataMap);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(caseDataMap)
                             .build())
            .build();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            editReturnedOrderController.handleEditAndReturnedSubmitted(authToken, s2sToken, callbackRequest);
        });
        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testSubmittedCallBack() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(caseDataMap)
                             .build())
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), caseDataMap, caseData, null);
        when(allTabsService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData, Event.EDIT_AND_APPROVE_ORDER.getId(), authToken)).thenReturn(caseDataMap);
        ResponseEntity<SubmittedCallbackResponse> responseEntity = editReturnedOrderController
            .handleEditAndReturnedSubmitted(authToken, s2sToken, callbackRequest);
        assertNotNull(responseEntity.getBody());
    }
}
