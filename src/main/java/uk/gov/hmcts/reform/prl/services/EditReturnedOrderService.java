package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.OrderStatusEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils.isHearingPageNeeded;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"java:S3776", "java:S6204"})
public class EditReturnedOrderService {

    private static final String BOLD_BEGIN = "<span class='heading-h2'>";
    private static final String BOLD_END = "</span>";
    private static final String INSTRUCTIONS_FROM_JUDGE = BOLD_BEGIN + "Instructions from the judge" + BOLD_END + " \n\n ";
    private static final String EDIT_THE_ORDER_LABEL = " \n\n <span class='heading-h3'>Edit the order</span> \n\n";
    public static final String OPEN_THE_DRAFT_ORDER_TEXT = "Open the draft order and edit it to include the judge's instructions.";
    public static final String USE_CONTINUE_TO_EDIT_THE_ORDER = "Use continue to edit the order.";
    public static final String ORDER_UPLOADED_AS_DRAFT_FLAG = "orderUploadedAsDraftFlag";
    public static final String ORDER_TYPE = "orderType";
    public static final String MANAGE_ORDER_OPTION_TYPE = "manageOrderOptionType";
    public static final String INSTRUCTIONS_TO_LEGAL_REPRESENTATIVE = "instructionsToLegalRepresentative";
    public static final String EDIT_ORDER_TEXT_INSTRUCTIONS = "editOrderTextInstructions";
    public static final String PREVIEW_UPLOADED_ORDER = "previewUploadedOrder";
    public static final String SELECTED_ORDER = "selectedOrder";
    private final ObjectMapper objectMapper;
    private final UserService userService;

    private static final String ORDER_NAME = "orderName";
    private static final String CASE_TYPE_OF_APPLICATION = "caseTypeOfApplication";
    private static final String IS_HEARING_PAGE_NEEDED = "isHearingPageNeeded";
    private static final String IS_ORDER_CREATED_BY_SOLICITOR = "isOrderCreatedBySolicitor";

    private final DraftAnOrderService draftAnOrderService;

    private final DynamicMultiSelectListService dynamicMultiSelectListService;

    public AboutToStartOrSubmitCallbackResponse handleAboutToStartCallback(String authorisation, CallbackRequest callbackRequest) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        if (caseData.getDraftOrderCollection() != null
            && !caseData.getDraftOrderCollection().isEmpty()) {
            Map<String, Object> caseDataUpdated = new HashMap<>();
            caseDataUpdated.put("rejectedOrdersDynamicList", getReturnedOrdersDynamicList(authorisation, caseData));
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated).build();
        } else {
            return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of("There are no draft orders")).build();
        }
    }

    public DynamicList getReturnedOrdersDynamicList(String authorisation, CaseData caseData) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        log.info("** UserDetails {}", userDetails);
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
        log.info("*** draftoo order collection : {}", caseData.getDraftOrderCollection());
        log.info("*** draft order list for drop down : {}", supportedDraftOrderList);
        return ElementUtils.asDynamicList(
            supportedDraftOrderList,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        );
    }

    public Map<String, Object> populateInstructionsAndDocuments(CaseData caseData) {
        Map<String, Object> caseDataMap = new HashMap<>();
        DraftOrder selectedOrder = draftAnOrderService.getSelectedDraftOrderDetails(caseData.getDraftOrderCollection(), caseData.getManageOrders()
            .getRejectedOrdersDynamicList());
        caseDataMap.put(ORDER_NAME, ManageOrdersUtils.getOrderName(selectedOrder));
        caseDataMap.put(ORDER_UPLOADED_AS_DRAFT_FLAG, selectedOrder.getIsOrderUploadedByJudgeOrAdmin());
        caseDataMap.put(ORDER_TYPE, selectedOrder.getOrderType());
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, caseData.getCaseTypeOfApplication());
        caseDataMap.put(MANAGE_ORDER_OPTION_TYPE, selectedOrder.getOrderSelectionType());
        caseDataMap.put(IS_ORDER_CREATED_BY_SOLICITOR, selectedOrder.getIsOrderCreatedBySolicitor());
        if (selectedOrder.getOtherDetails().getInstructionsToLegalRepresentative() != null) {
            caseDataMap.put(INSTRUCTIONS_TO_LEGAL_REPRESENTATIVE, selectedOrder.getOtherDetails().getInstructionsToLegalRepresentative());
        }
        caseDataMap.put(
            IS_HEARING_PAGE_NEEDED,
            isHearingPageNeeded(selectedOrder.getOrderType(), selectedOrder.getC21OrderOptions()) ? Yes : No
        );
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        if (YesOrNo.Yes.equals(selectedOrder.getIsOrderUploadedByJudgeOrAdmin())) {
            caseDataMap.put(EDIT_ORDER_TEXT_INSTRUCTIONS, INSTRUCTIONS_FROM_JUDGE
                    + selectedOrder.getOtherDetails().getInstructionsToLegalRepresentative()
                    + EDIT_THE_ORDER_LABEL + "\n" + OPEN_THE_DRAFT_ORDER_TEXT);
            caseDataMap.put(PREVIEW_UPLOADED_ORDER, selectedOrder.getOrderDocument());
            caseDataMap.put(SELECTED_ORDER, selectedOrder.getOrderTypeId());
        } else {
            caseDataMap.put(EDIT_ORDER_TEXT_INSTRUCTIONS, INSTRUCTIONS_FROM_JUDGE + "\n"
                    + selectedOrder.getOtherDetails().getInstructionsToLegalRepresentative()
                    + EDIT_THE_ORDER_LABEL + "\n" + USE_CONTINUE_TO_EDIT_THE_ORDER);
        }
        caseDataMap.put("isTheOrderAboutAllChildren", selectedOrder.getIsTheOrderAboutAllChildren());
        caseDataMap.put("isTheOrderAboutChildren", selectedOrder.getIsTheOrderAboutChildren());
        caseDataMap.put("childOption", (Yes.equals(selectedOrder.getIsTheOrderAboutChildren())
                || No.equals(selectedOrder.getIsTheOrderAboutAllChildren()))
                ? selectedOrder.getChildOption() : DynamicMultiSelectList.builder()
                .listItems(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).build());
        return caseDataMap;
    }
}
