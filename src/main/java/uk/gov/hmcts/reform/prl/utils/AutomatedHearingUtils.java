package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
public class AutomatedHearingUtils {

    private AutomatedHearingUtils() {

    }

    public static void automatedHearingManagementRequest(String authorisation,
                                                         CaseData caseData,
                                                         ManageOrderService manageOrderService) {
        log.info("Automated Hearing Management: automatedHearingManagementRequest: Start");
        if (CollectionUtils.isNotEmpty(caseData.getDraftOrderCollection())) {
            log.info("Automated Hearing Management: Triggering AHR for draft order");
            caseData.getDraftOrderCollection().stream().findFirst()
                .ifPresent(order -> {
                    if (Yes.equals(order.getValue().getIsAutoHearingReqPending())) {
                        log.info("Automated Hearing Management: Order is approved, checking for AHR");
                        order.getValue().setManageOrderHearingDetails(manageOrderService
                                                                          .createAutomatedHearingManagement(
                                                                              authorisation,
                                                                              caseData,
                                                                              order.getValue()
                                                                                  .getManageOrderHearingDetails()
                                                                          ));
                    }
                    order.getValue().setIsAutoHearingReqPending(No);
                });
        }

        if (CollectionUtils.isNotEmpty(caseData.getOrderCollection())) {
            log.info("Automated Hearing Management: Triggering AHR for order");
            caseData.getOrderCollection().stream().findFirst()
                .ifPresent(order -> {
                    if (Yes.equals(order.getValue().getIsAutoHearingReqPending())) {
                        log.info("Automated Hearing Management: Order is served/finalised, checking for AHR");
                        order.getValue().setManageOrderHearingDetails(manageOrderService
                                                                          .createAutomatedHearingManagement(
                                                                              authorisation, caseData, order.getValue()
                                                                                  .getManageOrderHearingDetails()
                                                                          ));
                    }
                    order.getValue().setIsAutoHearingReqPending(No);
                });
        }
        log.info("Automated Hearing Management: automatedHearingManagementRequest: End");
    }
}
