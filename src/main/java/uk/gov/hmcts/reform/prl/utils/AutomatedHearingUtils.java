package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;

import java.util.Map;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
public class AutomatedHearingUtils {

    private AutomatedHearingUtils() {

    }

    public static void automatedHearingManagementRequest(String authorisation,
                                                         CaseData caseData,
                                                         Map<String, Object> caseDataMap,
                                                         ManageOrderService manageOrderService,
                                                         String eventId) {
        log.info("Automated Hearing Management: automatedHearingManagementRequest: Start");
        //Judge or Manager approves the order
        if (Event.EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(eventId)) {
            log.info("Automated Hearing Management: Edit and approve order event");
            if (CollectionUtils.isNotEmpty(caseData.getDraftOrderCollection())) {
                caseData.getDraftOrderCollection().stream().findFirst()
                    .ifPresent(order -> {
                        if (Yes.equals(order.getValue().getIsAutoHearingReqPending())) {
                            log.info("Automated Hearing Management: Order is approved, checking for AHR");
                            order.getValue().setManageOrderHearingDetails(manageOrderService
                                                                              .createAutomatedHearingManagement(
                                                                                  authorisation, caseData, order.getValue()
                                                                                      .getManageOrderHearingDetails()
                                                                              ));
                        }
                        order.getValue().setIsAutoHearingReqPending(No);
                    });
            }
        }

        //Admin creates, edit and serve the order
        if (Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId().equalsIgnoreCase(eventId)
            || Event.MANAGE_ORDERS.getId().equalsIgnoreCase(eventId)) {
            log.info("Automated Hearing Management: Edit serve or finalize order event");
            if (CollectionUtils.isNotEmpty(caseData.getOrderCollection())) {
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
        }
        log.info("Automated Hearing Management: automatedHearingManagementRequest: End");
    }
}
