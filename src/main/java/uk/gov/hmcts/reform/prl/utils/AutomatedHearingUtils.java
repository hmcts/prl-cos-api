package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;

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
        log.info("Automated Hearing Management: automatedHearingManagementRequest: Start");
        if (CollectionUtils.isNotEmpty(caseData.getDraftOrderCollection())) {
            log.info("Automated Hearing Management: Triggering AHR for draft order");
            AtomicBoolean isAhrTriggered = new AtomicBoolean(false);
            caseData.getDraftOrderCollection().stream().findFirst()
                .ifPresent(order -> {
                    if (Yes.equals(order.getValue().getIsAutoHearingReqPending())) {
                        order.getValue().setManageOrderHearingDetails(manageOrderService
                                                                          .createAutomatedHearingManagement(
                                                                              authorisation,
                                                                              caseData,
                                                                              order.getValue()
                                                                                  .getManageOrderHearingDetails(),
                                                                              caseDataMap
                                                                          ));
                        isAhrTriggered.set(true);
                    }
                    log.info("Draft order collection, setting isAutoHearingReqPending to No");
                    order.getValue().setIsAutoHearingReqPending(No);
                });
            if (isAhrTriggered.get()) {
                log.info("AHR triggered, saving draft order collection to caseDataMap");
                caseDataMap.put(DRAFT_ORDER_COLLECTION, caseData.getDraftOrderCollection());
            }
        }

        if (CollectionUtils.isNotEmpty(caseData.getOrderCollection())) {
            log.info("Automated Hearing Management: Triggering AHR for order");
            AtomicBoolean isAhrTriggered = new AtomicBoolean(false);
            caseData.getOrderCollection().stream().findFirst()
                .ifPresent(order -> {
                    if (Yes.equals(order.getValue().getIsAutoHearingReqPending())) {
                        order.getValue().setManageOrderHearingDetails(manageOrderService
                                                                          .createAutomatedHearingManagement(
                                                                              authorisation, caseData, order.getValue()
                                                                                  .getManageOrderHearingDetails(),
                                                                              caseDataMap
                                                                          ));
                        isAhrTriggered.set(true);
                    }
                    log.info("Order collection, setting isAutoHearingReqPending to No");
                    order.getValue().setIsAutoHearingReqPending(No);
                });
            if (isAhrTriggered.get()) {
                log.info("AHR triggered, saving order collection to caseDataMap");
                caseDataMap.put(ORDER_COLLECTION, caseData.getOrderCollection());
            }
        }
        log.info("Automated Hearing Management: automatedHearingManagementRequest: End");
    }
}
