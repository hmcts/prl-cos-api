package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.controllers.editreturnedorder.EditReturnedOrderController;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.services.EditReturnedOrderService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@RunWith(MockitoJUnitRunner.Silent.class)
@PropertySource(value = "classpath:application.yaml")
public class EditReturnedOrderControllerTest {

    @Mock
    private  ObjectMapper objectMapper;
    @Mock
    private  DraftAnOrderService draftAnOrderService;

    @Mock
    private EditReturnedOrderService editReturnedOrderService;

    @InjectMocks
    private EditReturnedOrderController editReturnedOrderController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @Before
    public void setUp() {
        when(draftAnOrderService.getSelectedDraftOrderDetails(Mockito.any())).thenReturn(DraftOrder.builder().build());
    }

    @Test
    public void testGenerateReturnedOrdersDropdown() {
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
        Assert.assertTrue(response.getData().containsKey("rejectedOrdersDynamicList"));
    }

    @Test
    public void testInvaliddClientOnHandleAboutToStart() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData, Event.EDIT_AND_APPROVE_ORDER.getId())).thenReturn(caseDataMap);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(caseDataMap)
                             .build())
            .build();
        assertExpectedException(() -> {
            editReturnedOrderController.handleAboutToStart(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Ignore
    @Test
    public void testPopulateInstructions() {
        Element<DraftOrder> draftOrderElement = Element.<DraftOrder>builder().build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection.add(draftOrderElement);
        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .draftOrderCollection(draftOrderCollection)
            .state(State.CASE_ISSUED)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        caseDataMap.put("orderType", "test");
        when(draftAnOrderService.populateCommonDraftOrderFields(Mockito.anyString(),Mockito.any())).thenReturn(caseDataMap);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(caseDataMap)
                             .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = editReturnedOrderController
            .populateInstructionsToSolicitor(authToken,s2sToken,callbackRequest);
        Assert.assertFalse(response.getData().isEmpty());
    }

    @Test
    public void testPopulateInstructionsWithEmptyDraftOrderCollection() {
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
        when(draftAnOrderService.populateCommonDraftOrderFields(Mockito.anyString(), Mockito.any())).thenReturn(caseDataMap);
        AboutToStartOrSubmitCallbackResponse response = editReturnedOrderController
            .populateInstructionsToSolicitor(authToken,s2sToken,callbackRequest);
        Assert.assertTrue(response.getErrors().size() > 0);
    }

    @Test
    public void testInvaliddClientOnPopulateInstructionsCallBack() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData, Event.EDIT_AND_APPROVE_ORDER.getId())).thenReturn(caseDataMap);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(caseDataMap)
                             .build())
            .build();
        assertExpectedException(() -> {
            editReturnedOrderController.populateInstructionsToSolicitor(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testInvaliddClientOnSubmittedCallBack() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(draftAnOrderService.getDraftOrderDynamicList(caseData, Event.EDIT_AND_APPROVE_ORDER.getId())).thenReturn(caseDataMap);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(caseDataMap)
                             .build())
            .build();
        assertExpectedException(() -> {
            editReturnedOrderController.handleEditAndReturnedSubmitted(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testSubmittedCallBack() {
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
        when(draftAnOrderService.getDraftOrderDynamicList(caseData, Event.EDIT_AND_APPROVE_ORDER.getId())).thenReturn(caseDataMap);
        ResponseEntity<SubmittedCallbackResponse> responseEntity = editReturnedOrderController
            .handleEditAndReturnedSubmitted(authToken, s2sToken, callbackRequest);
        assertNotNull(responseEntity.getBody());
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
