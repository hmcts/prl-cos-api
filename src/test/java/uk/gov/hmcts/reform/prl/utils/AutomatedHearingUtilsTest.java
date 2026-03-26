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

        DraftOrder draftOrder = DraftOrder.builder()
            .isAutoHearingReqPending(Yes)
            .manageOrderHearingDetails(hearingDetails)
            .build();

        List<Element<DraftOrder>> draftOrders = new ArrayList<>();
        draftOrders.add(Element.<DraftOrder>builder().id(orderId).value(draftOrder).build());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderDoc", "some-doc");
        caseDataMap.put(DRAFT_ORDER_COLLECTION, draftOrders);

        when(manageOrderService.createAutomatedHearingManagement(eq(authorisation), eq(caseData), any()))
            .thenReturn(updatedHearingDetails);

        AutomatedHearingUtils.automatedHearingManagementRequest(
            authorisation, caseData, caseDataMap, manageOrderService, objectMapper);

        // Verify AHR was called
        verify(manageOrderService).createAutomatedHearingManagement(authorisation, caseData, hearingDetails);

        // Verify draft order collection was updated in caseDataMap
        assertNotNull(caseDataMap.get(DRAFT_ORDER_COLLECTION));
    }

    @Test
    void testAutomatedHearingManagementRequest_customOrder_orderCollection_withAhrPending() {
        // Setup order with AHR pending
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

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderDoc", "some-doc");
        caseDataMap.put(ORDER_COLLECTION, orders);

        when(manageOrderService.createAutomatedHearingManagement(eq(authorisation), eq(caseData), any()))
            .thenReturn(updatedHearingDetails);

        AutomatedHearingUtils.automatedHearingManagementRequest(
            authorisation, caseData, caseDataMap, manageOrderService, objectMapper);

        // Verify AHR was called
        verify(manageOrderService).createAutomatedHearingManagement(authorisation, caseData, hearingDetails);

        // Verify order collection was updated in caseDataMap
        assertNotNull(caseDataMap.get(ORDER_COLLECTION));
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
        caseDataMap.put("customOrderDoc", "some-doc");
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
        // Even if customOrderDoc is present, if objectMapper is null, use original flow
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
        caseDataMap.put("customOrderDoc", "some-doc");

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
}
