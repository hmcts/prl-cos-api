package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_COLLECTION;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
public class AutomatedHearingUtils {

    private AutomatedHearingUtils() {

    }

    public static void automatedHearingManagementRequest(String authorisation,
                                                         CaseData caseData,
                                                         Map<String, Object> caseDataMap,
                                                         ManageOrderService manageOrderService) {
        automatedHearingManagementRequest(authorisation, caseData, caseDataMap, manageOrderService, null);
    }

    public static void automatedHearingManagementRequest(String authorisation,
                                                         CaseData caseData,
                                                         Map<String, Object> caseDataMap,
                                                         ManageOrderService manageOrderService,
                                                         ObjectMapper objectMapper) {
        log.info("AutomatedHearingUtils::automatedHearingManagementRequest: Start");

        // For custom orders, process AHR using caseDataMap's collections to preserve
        // custom order updates (combined doc, orderTypeId) that would be lost if we
        // overwrote with caseData's collections
        if (caseDataMap.get("customOrderDoc") != null && objectMapper != null) {
            processAhrForCustomOrder(authorisation, caseData, caseDataMap, manageOrderService, objectMapper);
            log.info("AutomatedHearingUtils::automatedHearingManagementRequest: End (custom order)");
            return;
        }
        if (CollectionUtils.isNotEmpty(caseData.getDraftOrderCollection())) {
            AtomicBoolean isAhrTriggered = new AtomicBoolean(false);
            caseData.getDraftOrderCollection().stream().findFirst()
                .ifPresent(order -> {
                    if (Yes.equals(order.getValue().getIsAutoHearingReqPending())) {
                        order.getValue().setManageOrderHearingDetails(manageOrderService
                                                                          .createAutomatedHearingManagement(
                                                                              authorisation,
                                                                              caseData,
                                                                              order.getValue()
                                                                                  .getManageOrderHearingDetails()
                                                                          ));
                        isAhrTriggered.set(true);
                    }
                    order.getValue().setIsAutoHearingReqPending(No);
                });
            if (isAhrTriggered.get()) {
                log.info("AHR triggered for a draft order, saving draft order collection to caseDataMap");
                caseDataMap.put(DRAFT_ORDER_COLLECTION, caseData.getDraftOrderCollection());
            }
        }

        if (CollectionUtils.isNotEmpty(caseData.getOrderCollection())) {
            AtomicBoolean isAhrTriggered = new AtomicBoolean(false);
            caseData.getOrderCollection().stream().findFirst()
                .ifPresent(order -> {
                    if (Yes.equals(order.getValue().getIsAutoHearingReqPending())) {
                        order.getValue().setManageOrderHearingDetails(manageOrderService
                                                                          .createAutomatedHearingManagement(
                                                                              authorisation, caseData, order.getValue()
                                                                                  .getManageOrderHearingDetails()
                                                                          ));
                        isAhrTriggered.set(true);
                    }
                    order.getValue().setIsAutoHearingReqPending(No);
                });
            if (isAhrTriggered.get()) {
                log.info("AHR triggered for an order, saving order collection to caseDataMap");
                caseDataMap.put(ORDER_COLLECTION, caseData.getOrderCollection());
            }
        }
        log.info("AutomatedHearingUtils::automatedHearingManagementRequest: End");
    }

    private static void processAhrForCustomOrder(String authorisation,
                                                 CaseData caseData,
                                                 Map<String, Object> caseDataMap,
                                                 ManageOrderService manageOrderService,
                                                 ObjectMapper objectMapper) {
        // Process draft orders from caseDataMap
        if (caseDataMap.get(DRAFT_ORDER_COLLECTION) != null) {
            List<Element<DraftOrder>> draftOrders = objectMapper.convertValue(
                caseDataMap.get(DRAFT_ORDER_COLLECTION), new TypeReference<>() {});

            if (CollectionUtils.isNotEmpty(draftOrders)) {
                AtomicBoolean isAhrTriggered = new AtomicBoolean(false);
                draftOrders.stream().findFirst()
                    .ifPresent(order -> {
                        if (Yes.equals(order.getValue().getIsAutoHearingReqPending())) {
                            order.getValue().setManageOrderHearingDetails(manageOrderService
                                .createAutomatedHearingManagement(
                                    authorisation,
                                    caseData,
                                    order.getValue().getManageOrderHearingDetails()
                                ));
                            isAhrTriggered.set(true);
                        }
                        order.getValue().setIsAutoHearingReqPending(No);
                    });
                if (isAhrTriggered.get()) {
                    log.info("AHR triggered for a custom draft order");
                }
                caseDataMap.put(DRAFT_ORDER_COLLECTION, draftOrders);
            }
        }

        // Process final orders from caseDataMap
        if (caseDataMap.get(ORDER_COLLECTION) != null) {
            List<Element<OrderDetails>> orders = objectMapper.convertValue(
                caseDataMap.get(ORDER_COLLECTION), new TypeReference<>() {});

            if (CollectionUtils.isNotEmpty(orders)) {
                AtomicBoolean isAhrTriggered = new AtomicBoolean(false);
                orders.stream().findFirst()
                    .ifPresent(order -> {
                        if (Yes.equals(order.getValue().getIsAutoHearingReqPending())) {
                            order.getValue().setManageOrderHearingDetails(manageOrderService
                                .createAutomatedHearingManagement(
                                    authorisation,
                                    caseData,
                                    order.getValue().getManageOrderHearingDetails()
                                ));
                            isAhrTriggered.set(true);
                        }
                        order.getValue().setIsAutoHearingReqPending(No);
                    });
                if (isAhrTriggered.get()) {
                    log.info("AHR triggered for a custom order");
                }
                caseDataMap.put(ORDER_COLLECTION, orders);
            }
        }
    }
}
