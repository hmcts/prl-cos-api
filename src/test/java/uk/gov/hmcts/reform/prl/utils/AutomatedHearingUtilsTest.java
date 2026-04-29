package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_COLLECTION;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class AutomatedHearingUtilsTest {

    @Mock
    private ManageOrderService manageOrderService;

    private ObjectMapper objectMapper;
    private CaseData caseData;
    private final String authorisation = "test-auth";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());
        caseData = CaseData.builder().build();
    }

    @Test
    void testAutomatedHearingManagementRequest_customOrder_draftOrderCollection_withAhrPending() {
        // Setup draft order with AHR pending
        // This test verifies that for custom orders, we read from caseDataMap (current/updated)
        // and NOT from caseData (stale database). The order in caseDataMap should be preserved.
        UUID orderId = UUID.randomUUID();
        List<Element<HearingData>> hearingDetails = new ArrayList<>();
        hearingDetails.add(Element.<HearingData>builder()
            .id(UUID.randomUUID())
            .value(HearingData.builder().build())
            .build());

        List<Element<HearingData>> updatedHearingDetails = new ArrayList<>();
        updatedHearingDetails.add(Element.<HearingData>builder()
            .id(UUID.randomUUID())
            .value(HearingData.builder().build())
            .build());

        // caseDataMap has the CURRENT draft order (e.g., with combined document)
        DraftOrder currentDraftOrder = DraftOrder.builder()
            .isAutoHearingReqPending(Yes)
            .manageOrderHearingDetails(hearingDetails)
            .orderTypeId("Current Draft Order - COMBINED")
            .build();

        List<Element<DraftOrder>> currentDraftOrders = new ArrayList<>();
        currentDraftOrders.add(Element.<DraftOrder>builder().id(orderId).value(currentDraftOrder).build());

        // caseData has STALE draft order (e.g., without combined document from database)
        DraftOrder staleDraftOrder = DraftOrder.builder()
            .isAutoHearingReqPending(Yes)
            .manageOrderHearingDetails(hearingDetails)
            .orderTypeId("Stale Draft Order - UNCOMBINED")
            .build();

        List<Element<DraftOrder>> staleDraftOrders = new ArrayList<>();
        staleDraftOrders.add(Element.<DraftOrder>builder().id(orderId).value(staleDraftOrder).build());

        caseData = CaseData.builder()
            .draftOrderCollection(staleDraftOrders)
            .build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "blankOrderOrDirections");
        caseDataMap.put(DRAFT_ORDER_COLLECTION, currentDraftOrders);

        when(manageOrderService.createAutomatedHearingManagement(eq(authorisation), eq(caseData), any()))
            .thenReturn(updatedHearingDetails);

        AutomatedHearingUtils.automatedHearingManagementRequest(
            authorisation, caseData, caseDataMap, manageOrderService, objectMapper);

        // Verify AHR was called
        verify(manageOrderService).createAutomatedHearingManagement(authorisation, caseData, hearingDetails);

        // Verify draft order collection was updated in caseDataMap with correct content
        // CRITICAL: Should have "Current Draft Order - COMBINED", NOT "Stale Draft Order - UNCOMBINED"
        @SuppressWarnings("unchecked")
        List<Element<DraftOrder>> resultDraftOrders = (List<Element<DraftOrder>>) caseDataMap.get(DRAFT_ORDER_COLLECTION);
        assertNotNull(resultDraftOrders);
        assertEquals(1, resultDraftOrders.size());
        assertEquals(orderId, resultDraftOrders.get(0).getId());
        assertEquals("Current Draft Order - COMBINED", resultDraftOrders.get(0).getValue().getOrderTypeId(),
            "Draft order should be from caseDataMap (current), not caseData (stale)");
        // isAutoHearingReqPending should be set to No after processing
        assertEquals(No, resultDraftOrders.get(0).getValue().getIsAutoHearingReqPending());
        // manageOrderHearingDetails should be updated with the returned value
        assertEquals(updatedHearingDetails, resultDraftOrders.get(0).getValue().getManageOrderHearingDetails());
    }

    @Test
    void testAutomatedHearingManagementRequest_customOrder_orderCollection_withAhrPending() {
        // Setup order with AHR pending
        // This test verifies that for custom orders, we read from caseDataMap (current/updated)
        // and NOT from caseData (stale database). The order in caseDataMap should be preserved.
        UUID orderId = UUID.randomUUID();
        List<Element<HearingData>> hearingDetails = new ArrayList<>();
        hearingDetails.add(Element.<HearingData>builder()
            .id(UUID.randomUUID())
            .value(HearingData.builder().build())
            .build());

        List<Element<HearingData>> updatedHearingDetails = new ArrayList<>();
        updatedHearingDetails.add(Element.<HearingData>builder()
            .id(UUID.randomUUID())
            .value(HearingData.builder().build())
            .build());

        // caseDataMap has the CURRENT order (e.g., sealed/combined document)
        OrderDetails currentOrderDetails = OrderDetails.builder()
            .isAutoHearingReqPending(Yes)
            .manageOrderHearingDetails(hearingDetails)
            .orderTypeId("Current Order - SEALED")
            .build();

        List<Element<OrderDetails>> currentOrders = new ArrayList<>();
        currentOrders.add(Element.<OrderDetails>builder().id(orderId).value(currentOrderDetails).build());

        // caseData has STALE order (e.g., unsealed document from database)
        OrderDetails staleOrderDetails = OrderDetails.builder()
            .isAutoHearingReqPending(Yes)
            .manageOrderHearingDetails(hearingDetails)
            .orderTypeId("Stale Order - UNSEALED")
            .build();

        List<Element<OrderDetails>> staleOrders = new ArrayList<>();
        staleOrders.add(Element.<OrderDetails>builder().id(orderId).value(staleOrderDetails).build());

        caseData = CaseData.builder()
            .orderCollection(staleOrders)
            .build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "blankOrderOrDirections");
        caseDataMap.put(ORDER_COLLECTION, currentOrders);

        when(manageOrderService.createAutomatedHearingManagement(eq(authorisation), eq(caseData), any()))
            .thenReturn(updatedHearingDetails);

        AutomatedHearingUtils.automatedHearingManagementRequest(
            authorisation, caseData, caseDataMap, manageOrderService, objectMapper);

        // Verify AHR was called
        verify(manageOrderService).createAutomatedHearingManagement(authorisation, caseData, hearingDetails);

        // Verify order collection was updated in caseDataMap with correct content
        // CRITICAL: Should have "Current Order - SEALED", NOT "Stale Order - UNSEALED"
        @SuppressWarnings("unchecked")
        List<Element<OrderDetails>> resultOrders = (List<Element<OrderDetails>>) caseDataMap.get(ORDER_COLLECTION);
        assertNotNull(resultOrders);
        assertEquals(1, resultOrders.size());
        assertEquals(orderId, resultOrders.get(0).getId());
        assertEquals("Current Order - SEALED", resultOrders.get(0).getValue().getOrderTypeId(),
            "Order should be from caseDataMap (current), not caseData (stale)");
        // isAutoHearingReqPending should be set to No after processing
        assertEquals(No, resultOrders.get(0).getValue().getIsAutoHearingReqPending());
        // manageOrderHearingDetails should be updated with the returned value
        assertEquals(updatedHearingDetails, resultOrders.get(0).getValue().getManageOrderHearingDetails());
    }

    @Test
    void testAutomatedHearingManagementRequest_customOrder_noAhrPending() {
        // Setup order without AHR pending
        UUID orderId = UUID.randomUUID();
        OrderDetails orderDetails = OrderDetails.builder()
            .isAutoHearingReqPending(No)
            .build();

        List<Element<OrderDetails>> orders = new ArrayList<>();
        orders.add(Element.<OrderDetails>builder().id(orderId).value(orderDetails).build());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "blankOrderOrDirections");
        caseDataMap.put(ORDER_COLLECTION, orders);

        AutomatedHearingUtils.automatedHearingManagementRequest(
            authorisation, caseData, caseDataMap, manageOrderService, objectMapper);

        // Verify AHR was not called
        verify(manageOrderService, never()).createAutomatedHearingManagement(any(), any(), any());

        // Verify order collection still in caseDataMap with isAutoHearingReqPending set to No
        assertNotNull(caseDataMap.get(ORDER_COLLECTION));
    }

    @Test
    void testAutomatedHearingManagementRequest_nonCustomOrder_usesOriginalFlow() {
        // Setup order with AHR pending on caseData (non-custom order flow)
        UUID orderId = UUID.randomUUID();
        List<Element<HearingData>> hearingDetails = new ArrayList<>();
        hearingDetails.add(Element.<HearingData>builder()
            .id(UUID.randomUUID())
            .value(HearingData.builder().build())
            .build());

        List<Element<HearingData>> updatedHearingDetails = new ArrayList<>();
        updatedHearingDetails.add(Element.<HearingData>builder()
            .id(UUID.randomUUID())
            .value(HearingData.builder().build())
            .build());

        OrderDetails orderDetails = OrderDetails.builder()
            .isAutoHearingReqPending(Yes)
            .manageOrderHearingDetails(hearingDetails)
            .build();

        List<Element<OrderDetails>> orders = new ArrayList<>();
        orders.add(Element.<OrderDetails>builder().id(orderId).value(orderDetails).build());

        caseData = CaseData.builder()
            .orderCollection(orders)
            .build();

        Map<String, Object> caseDataMap = new HashMap<>();
        // No customOrderDoc - so original flow

        when(manageOrderService.createAutomatedHearingManagement(eq(authorisation), eq(caseData), any()))
            .thenReturn(updatedHearingDetails);

        AutomatedHearingUtils.automatedHearingManagementRequest(
            authorisation, caseData, caseDataMap, manageOrderService, objectMapper);

        // Verify AHR was called
        verify(manageOrderService).createAutomatedHearingManagement(authorisation, caseData, hearingDetails);

        // Verify order collection from caseData was put into caseDataMap
        assertNotNull(caseDataMap.get(ORDER_COLLECTION));
    }

    @Test
    void testAutomatedHearingManagementRequest_backwardCompatibleMethod() {
        // Test the overloaded method without objectMapper
        UUID orderId = UUID.randomUUID();
        List<Element<HearingData>> hearingDetails = new ArrayList<>();
        hearingDetails.add(Element.<HearingData>builder()
            .id(UUID.randomUUID())
            .value(HearingData.builder().build())
            .build());

        List<Element<HearingData>> updatedHearingDetails = new ArrayList<>();
        updatedHearingDetails.add(Element.<HearingData>builder()
            .id(UUID.randomUUID())
            .value(HearingData.builder().build())
            .build());

        OrderDetails orderDetails = OrderDetails.builder()
            .isAutoHearingReqPending(Yes)
            .manageOrderHearingDetails(hearingDetails)
            .build();

        List<Element<OrderDetails>> orders = new ArrayList<>();
        orders.add(Element.<OrderDetails>builder().id(orderId).value(orderDetails).build());

        caseData = CaseData.builder()
            .orderCollection(orders)
            .build();

        Map<String, Object> caseDataMap = new HashMap<>();

        when(manageOrderService.createAutomatedHearingManagement(eq(authorisation), eq(caseData), any()))
            .thenReturn(updatedHearingDetails);

        // Call without objectMapper
        AutomatedHearingUtils.automatedHearingManagementRequest(
            authorisation, caseData, caseDataMap, manageOrderService);

        // Verify AHR was called
        verify(manageOrderService).createAutomatedHearingManagement(authorisation, caseData, hearingDetails);
    }

    @Test
    void testAutomatedHearingManagementRequest_customOrder_withNullObjectMapper_usesOriginalFlow() {
        // Even if customOrderNameOption is present, if objectMapper is null, use original flow
        UUID orderId = UUID.randomUUID();
        List<Element<HearingData>> hearingDetails = new ArrayList<>();
        hearingDetails.add(Element.<HearingData>builder()
            .id(UUID.randomUUID())
            .value(HearingData.builder().build())
            .build());

        List<Element<HearingData>> updatedHearingDetails = new ArrayList<>();
        updatedHearingDetails.add(Element.<HearingData>builder()
            .id(UUID.randomUUID())
            .value(HearingData.builder().build())
            .build());

        OrderDetails orderDetails = OrderDetails.builder()
            .isAutoHearingReqPending(Yes)
            .manageOrderHearingDetails(hearingDetails)
            .build();

        List<Element<OrderDetails>> orders = new ArrayList<>();
        orders.add(Element.<OrderDetails>builder().id(orderId).value(orderDetails).build());

        caseData = CaseData.builder()
            .orderCollection(orders)
            .build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "blankOrderOrDirections");

        when(manageOrderService.createAutomatedHearingManagement(eq(authorisation), eq(caseData), any()))
            .thenReturn(updatedHearingDetails);

        // Call with null objectMapper
        AutomatedHearingUtils.automatedHearingManagementRequest(
            authorisation, caseData, caseDataMap, manageOrderService, null);

        // Verify AHR was called (original flow, not custom order flow)
        verify(manageOrderService).createAutomatedHearingManagement(authorisation, caseData, hearingDetails);

        // Original flow updates caseDataMap from caseData's collection
        assertNotNull(caseDataMap.get(ORDER_COLLECTION));
    }

    @Test
    void testAutomatedHearingManagementRequest_staleCustomOrderDoc_shouldNotTriggerCustomOrderFlow() {
        // This test verifies that stale customOrderDoc from a previous order
        // does NOT trigger the custom order flow. Only customOrderNameOption
        // (which indicates user selected custom order in THIS flow) should trigger it.

        UUID orderId = UUID.randomUUID();
        List<Element<HearingData>> hearingDetails = new ArrayList<>();
        hearingDetails.add(Element.<HearingData>builder()
            .id(UUID.randomUUID())
            .value(HearingData.builder().build())
            .build());

        List<Element<HearingData>> updatedHearingDetails = new ArrayList<>();
        updatedHearingDetails.add(Element.<HearingData>builder()
            .id(UUID.randomUUID())
            .value(HearingData.builder().build())
            .build());

        OrderDetails orderDetails = OrderDetails.builder()
            .isAutoHearingReqPending(Yes)
            .manageOrderHearingDetails(hearingDetails)
            .build();

        List<Element<OrderDetails>> orders = new ArrayList<>();
        orders.add(Element.<OrderDetails>builder().id(orderId).value(orderDetails).build());

        caseData = CaseData.builder()
            .orderCollection(orders)
            .build();

        Map<String, Object> caseDataMap = new HashMap<>();
        // Stale customOrderDoc from previous order - should NOT trigger custom order flow
        caseDataMap.put("customOrderDoc", "stale-doc-from-previous-order");
        // NO customOrderNameOption - this is an upload order, not custom order

        when(manageOrderService.createAutomatedHearingManagement(eq(authorisation), eq(caseData), any()))
            .thenReturn(updatedHearingDetails);

        AutomatedHearingUtils.automatedHearingManagementRequest(
            authorisation, caseData, caseDataMap, manageOrderService, objectMapper);

        // Verify AHR was called using the non-custom order flow (reads from caseData)
        verify(manageOrderService).createAutomatedHearingManagement(authorisation, caseData, hearingDetails);

        // Verify order collection was updated in caseDataMap (from non-custom flow)
        assertNotNull(caseDataMap.get(ORDER_COLLECTION));
    }
}
