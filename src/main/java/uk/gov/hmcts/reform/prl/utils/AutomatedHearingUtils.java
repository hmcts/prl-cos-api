package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;

import java.util.Map;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
public class AutomatedHearingUtils {

    private AutomatedHearingUtils() {

    }

    public static void automatedHearingManagementRequest(String authorisation, CaseData caseData, Map<String, Object> caseDataMap,
                                                   ManageOrderService manageOrderService) {
        log.info("Automated Hearing Management: automatedHearingManagementRequest: Start");
        boolean saveAsDraft = No.getDisplayedValue().equalsIgnoreCase(String.valueOf(caseDataMap.get("doYouWantToServeOrder")));
        if (caseData.getManageOrders() != null) {
            if (caseData.getDraftOrderCollection() != null && !caseData.getDraftOrderCollection().isEmpty()
                && (!AmendOrderCheckEnum.noCheck.toString()
                .equalsIgnoreCase(String.valueOf(caseDataMap.get("amendOrderSelectCheckOptions"))) || saveAsDraft)) {
                log.info("Automated Hearing Management: amendOrderSelectCheckOptions: {}, saveAsDraft: {}",
                         caseData.getManageOrders().getAmendOrderSelectCheckOptions(), saveAsDraft
                );
                caseData.getDraftOrderCollection().forEach(draftOrder -> {
                    if (Yes.equals(draftOrder.getValue().getIsAutoHearingReqPending())) {
                        draftOrder.getValue().setManageOrderHearingDetails(manageOrderService
                            .createAutomatedHearingManagement(authorisation, caseData, draftOrder.getValue().getManageOrderHearingDetails()));
                    }
                    draftOrder.getValue().setIsAutoHearingReqPending(No);
                });
                caseDataMap.put("draftOrderCollection", caseData.getDraftOrderCollection());
            } else if (caseData.getOrderCollection() != null && !caseData.getOrderCollection().isEmpty()) {
                log.info("Automated Hearing Management: amendOrderSelectCheckOptions: {}, saveAsDraft: {}",
                         caseData.getManageOrders().getAmendOrderSelectCheckOptions(), saveAsDraft
                );
                caseData.getOrderCollection().forEach(order -> {
                    if (Yes.equals(order.getValue().getIsAutoHearingReqPending())) {
                        order.getValue().setManageOrderHearingDetails(manageOrderService
                            .createAutomatedHearingManagement(authorisation, caseData, order.getValue()
                                .getManageOrderHearingDetails()));
                    }
                    order.getValue().setIsAutoHearingReqPending(No);
                });
                caseDataMap.put("orderCollection", caseData.getOrderCollection());
            }
        }
        log.info("Automated Hearing Management: automatedHearingManagementRequest: End");
    }
}
