package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.OrderStatusEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils.isHearingPageNeeded;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"java:S3776", "java:S6204"})
public class EditReturnedOrderService {

    private final ElementUtils elementUtils;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    private static final String ORDER_NAME = "orderName";
    private static final String CASE_TYPE_OF_APPLICATION = "caseTypeOfApplication";
    private static final String IS_HEARING_PAGE_NEEDED = "isHearingPageNeeded";
    private static final String IS_ORDER_CREATED_BY_SOLICITOR = "isOrderCreatedBySolicitor";

    public Map<String, Object> getReturnedOrdersDynamicList(String authorisation, CaseData caseData) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        log.info("** UserDetails {}", userDetails);
        Map<String, Object> caseDataMap = new HashMap<>();
        List<Element<DraftOrder>> supportedDraftOrderList = new ArrayList<>();
        caseData.getDraftOrderCollection().forEach(
            draftOrderElement -> {
                log.info("** order created by email {}", draftOrderElement.getValue().getOtherDetails().getOrderCreatedByEmailId());
                if (OrderStatusEnum.rejectedByJudge.getDisplayedValue()
                    .equalsIgnoreCase(draftOrderElement.getValue().getOtherDetails().getStatus())

                    && userDetails.getEmail().equalsIgnoreCase(draftOrderElement.getValue().getOtherDetails().getOrderCreatedByEmailId())) {
                    supportedDraftOrderList.add(draftOrderElement);
                }
            }
        );
        caseDataMap.put("rejectedOrdersDynamicList", ElementUtils.asDynamicList(
            supportedDraftOrderList,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));
        log.info("*** rejected orders dynamic list : {}", caseDataMap.get("rejectedOrdersDynamicList"));
        log.info("*** draftoo order collection : {}", caseData.getDraftOrderCollection());
        return caseDataMap;
    }

    public Map<String, Object> populateInstructionsAndDocuments(CaseData caseData) {
        Map<String, Object> caseDataMap = new HashMap<>();
        DraftOrder selectedOrder = getSelectedDraftOrderDetails(caseData.getDraftOrderCollection(), caseData.getManageOrders()
            .getRejectedOrdersDynamicList());
        caseDataMap.put(ORDER_NAME, ManageOrdersUtils.getOrderName(selectedOrder));
        caseDataMap.put("orderUploadedAsDraftFlag", selectedOrder.getIsOrderUploadedByJudgeOrAdmin());
        caseDataMap.put("orderType", selectedOrder.getOrderType());
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, caseData.getCaseTypeOfApplication());
        caseDataMap.put("manageOrderOptionType", selectedOrder.getOrderSelectionType());
        caseDataMap.put(IS_ORDER_CREATED_BY_SOLICITOR, selectedOrder.getIsOrderCreatedBySolicitor());
        if (selectedOrder.getOtherDetails().getInstructionsToLegalRepresentative() != null) {
            caseDataMap.put("instructionsToLegalRepresentative", selectedOrder.getOtherDetails().getInstructionsToLegalRepresentative());
        }
        caseDataMap.put(
            IS_HEARING_PAGE_NEEDED,
            isHearingPageNeeded(selectedOrder.getOrderType(), selectedOrder.getC21OrderOptions()) ? Yes : No
        );
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        if (YesOrNo.Yes.equals(selectedOrder.getIsOrderUploadedByJudgeOrAdmin())) {
            caseDataMap.put("editOrderTextInstructions", "Open the draft order and edit it to include the judge's instructions");
            caseDataMap.put("previewUploadedOrder", selectedOrder.getOrderDocument());
        } else {
            caseDataMap.put("editOrderTextInstructions", "Use continue to edit the order");
        }
        return caseDataMap;
    }

    public DraftOrder getSelectedDraftOrderDetails(List<Element<DraftOrder>> draftOrderCollection, Object dynamicList) {
        UUID orderId = elementUtils.getDynamicListSelectedValue(
            dynamicList, objectMapper);
        log.info("** Order id {}", orderId);
        return draftOrderCollection.stream()
            .filter(element -> element.getId().equals(orderId))
            .map(Element::getValue)
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException("Could not find order"));
    }
}
