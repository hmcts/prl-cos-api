package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;

@Slf4j
public class AutomatedHearingUtils {

    private AutomatedHearingUtils() {

    }

    public static void automatedHearingManagementRequest(String authorisation, CaseData caseData,
                                                   ManageOrderService manageOrderService) {
        log.info("Automated Hearing Management: automatedHearingManagementRequest: Start");
        boolean saveAsDraft = ObjectUtils.isNotEmpty(caseData.getServeOrderData())
            && No.equals(caseData.getServeOrderData().getDoYouWantToServeOrder());
        if (caseData.getManageOrders() != null) {
            if (caseData.getDraftOrderCollection() != null && !caseData.getDraftOrderCollection().isEmpty()
                && (!AmendOrderCheckEnum.noCheck.equals(caseData.getManageOrders().getAmendOrderSelectCheckOptions()) || saveAsDraft)) {
                log.info("Automated Hearing Management: amendOrderSelectCheckOptions: {}, saveAsDraft: {}",
                         caseData.getManageOrders().getAmendOrderSelectCheckOptions(), saveAsDraft
                );
                caseData.getDraftOrderCollection().forEach(response -> {
                    if (Boolean.TRUE.equals(response.getValue().isAutoHearingReqPending())) {
                        manageOrderService.createAutomatedHearingManagement(authorisation, caseData);
                    }
                });
            } else if (caseData.getOrderCollection() != null && !caseData.getOrderCollection().isEmpty()) {
                log.info("Automated Hearing Management: amendOrderSelectCheckOptions: {}, saveAsDraft: {}",
                         caseData.getManageOrders().getAmendOrderSelectCheckOptions(), saveAsDraft
                );
                caseData.getOrderCollection().forEach(response -> {
                    if (Boolean.TRUE.equals(response.getValue().isAutoHearingReqPending())) {
                        manageOrderService.createAutomatedHearingManagement(authorisation, caseData);
                    }
                });
            }
        }
        log.info("Automated Hearing Management: automatedHearingManagementRequest: End");
    }
}
