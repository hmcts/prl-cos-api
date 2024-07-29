package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.OrderStatusEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_ORDER_NAME_SOLICITOR_CREATED;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils.isHearingPageNeeded;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"java:S3776", "java:S6204"})
public class EditReturnedOrderService {

    private static final String BOLD_BEGIN = "<span class='heading-h2'>";
    private static final String BOLD_END = "</span>";
    private static final String INSTRUCTIONS_FROM_JUDGE = BOLD_BEGIN + "Instructions from the judge" + BOLD_END + " \n\n ";
    private static final String EDIT_THE_ORDER_LABEL = "\n\n <span class='heading-h3'>Edit the order</span> \n";
    public static final String OPEN_THE_DRAFT_ORDER_TEXT = "Open the draft order and edit it to include the judge's instructions.";
    public static final String USE_CONTINUE_TO_EDIT_THE_ORDER = "Use continue to edit the order.";
    public static final String ORDER_UPLOADED_AS_DRAFT_FLAG = "orderUploadedAsDraftFlag";
    public static final String ORDER_TYPE = "orderType";
    public static final String MANAGE_ORDER_OPTION_TYPE = "manageOrderOptionType";
    public static final String INSTRUCTIONS_TO_LEGAL_REPRESENTATIVE = "instructionsToLegalRepresentative";
    public static final String EDIT_ORDER_TEXT_INSTRUCTIONS = "editOrderTextInstructions";
    public static final String PREVIEW_UPLOADED_ORDER = "previewUploadedOrder";
    public static final String SELECTED_ORDER = "selectedOrder";
    public static final String DRAFT_ORDER_COLLECTION = "draftOrderCollection";
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final ManageOrderService manageOrderService;

    private static final String ORDER_NAME = "orderName";
    private static final String CASE_TYPE_OF_APPLICATION = "caseTypeOfApplication";
    private static final String IS_HEARING_PAGE_NEEDED = "isHearingPageNeeded";
    private static final String IS_ORDER_CREATED_BY_SOLICITOR = "isOrderCreatedBySolicitor";

    private final DraftAnOrderService draftAnOrderService;
    private final HearingDataService hearingDataService;
    private final ElementUtils elementUtils;

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
        List<Element<DraftOrder>> supportedDraftOrderList = new ArrayList<>();
        caseData.getDraftOrderCollection().forEach(
            draftOrderElement -> {
                if (OrderStatusEnum.rejectedByJudge.getDisplayedValue()
                    .equalsIgnoreCase(draftOrderElement.getValue().getOtherDetails().getStatus())

                    && userDetails.getEmail().equalsIgnoreCase(draftOrderElement.getValue().getOtherDetails().getOrderCreatedByEmailId())) {
                    supportedDraftOrderList.add(draftOrderElement);
                }
            }
        );
        return ElementUtils.asDynamicList(
            supportedDraftOrderList,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        );
    }

    public Map<String, Object> populateInstructionsAndDocuments(CaseData caseData, DraftOrder selectedOrder) {
        Map<String, Object> caseDataMap = new HashMap<>();
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
        if (ManageOrdersOptionsEnum.uploadAnOrder.toString().equalsIgnoreCase(selectedOrder.getOrderSelectionType())) {
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
        return caseDataMap;
    }

    public Map<String,Object> updateDraftOrderCollection(CaseData caseData, String authorisation) {
        Map<String,Object> caseDataMap = new HashMap<>();
        List<Element<DraftOrder>> draftOrderCollection = caseData.getDraftOrderCollection();
        DraftOrder draftOrder = draftAnOrderService.getSelectedDraftOrderDetails(caseData.getDraftOrderCollection(),
                                                                                 caseData.getManageOrders().getRejectedOrdersDynamicList());

        if (ManageOrdersOptionsEnum.uploadAnOrder.toString().equalsIgnoreCase(draftOrder.getOrderSelectionType())) {
            DraftOrder updatedOrder = updateUploadedDraftOrderDetails(caseData, draftOrder);
            UUID selectedOrderId = elementUtils.getDynamicListSelectedValue(
                caseData.getManageOrders().getRejectedOrdersDynamicList(), objectMapper);
            for (Element<DraftOrder> e : caseData.getDraftOrderCollection()) {
                if (e.getId().equals(selectedOrderId)) {
                    draftOrderCollection.set(
                        draftOrderCollection.indexOf(e),
                        element(selectedOrderId, updatedOrder)
                    );
                    break;
                }
            }
            draftOrderCollection.sort(Comparator.comparing(
                m -> m.getValue().getOtherDetails().getDateCreated(),
                Comparator.reverseOrder()
            ));
            caseDataMap.put(DRAFT_ORDER_COLLECTION, draftOrderCollection);
        } else {
            caseDataMap.putAll(draftAnOrderService.updateDraftOrderCollection(caseData,authorisation, Event.EDIT_RETURNED_ORDER.getId()));
        }
        caseDataMap.put(WA_ORDER_NAME_SOLICITOR_CREATED, draftOrder.getLabelForOrdersDynamicList());
        return caseDataMap;
    }

    public DraftOrder updateUploadedDraftOrderDetails(CaseData caseData, DraftOrder draftOrder) {

        return draftOrder.toBuilder()
            .orderDocument(caseData.getUploadOrderDoc())
            .isTheOrderAboutChildren(caseData.getManageOrders().getIsTheOrderAboutChildren())
            .isTheOrderAboutAllChildren(caseData.getManageOrders().getIsTheOrderAboutAllChildren())
            .childOption(manageOrderService.getChildOption(caseData))
            .childrenList(caseData.getManageOrders() != null
                              ? manageOrderService.getSelectedChildInfoFromMangeOrder(caseData) : null)
            .otherDetails(draftOrder.getOtherDetails().toBuilder()
                              .status(manageOrderService.getOrderStatus(null, null,
                                                                        Event.EDIT_RETURNED_ORDER.getId(), null))
                              .build())
            .dateOrderMade(caseData.getDateOrderMade())
            .wasTheOrderApprovedAtHearing(caseData.getWasTheOrderApprovedAtHearing())
            .hearingsType(caseData.getManageOrders().getHearingsType())
            .build();
    }

    public AboutToStartOrSubmitCallbackResponse populateInstructionsAndFieldsForLegalRep(String authorisation, CallbackRequest callbackRequest) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        if (caseData.getDraftOrderCollection() != null
            && !caseData.getDraftOrderCollection().isEmpty()) {
            DraftOrder selectedOrder = draftAnOrderService.getSelectedDraftOrderDetails(caseData.getDraftOrderCollection(),
                                                                                        caseData.getManageOrders()
                                                                                            .getRejectedOrdersDynamicList());
            Map<String, Object> caseDataUpdated = populateInstructionsAndDocuments(caseData, selectedOrder);
            caseDataUpdated.putAll(draftAnOrderService.populateCommonDraftOrderFields(authorisation, caseData, selectedOrder));
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated).build();
        } else {
            return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of("There are no draft orders")).build();
        }
    }
}
