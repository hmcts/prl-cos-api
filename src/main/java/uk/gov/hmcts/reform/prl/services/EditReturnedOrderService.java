package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.OrderStatusEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.PartiesListGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"java:S3776", "java:S6204"})
public class EditReturnedOrderService {

    private static final String SOLICITOR_ORDERS_HEARING_DETAILS = "solicitorOrdersHearingDetails";
    private static final String ORDERS_HEARING_DETAILS = "ordersHearingDetails";
    public static final String SDO_INSTRUCTIONS_FILING_PARTIES_DYNAMIC_LIST = "sdoInstructionsFilingPartiesDynamicList";
    public static final String SDO_NEW_PARTNER_PARTIES_CAFCASS = "sdoNewPartnerPartiesCafcass";
    public static final String SDO_NEW_PARTNER_PARTIES_CAFCASS_CYMRU = "sdoNewPartnerPartiesCafcassCymru";
    private static final String DATE_ORDER_MADE = "dateOrderMade";
    private static final String SELECTED_ORDER = "selectedOrder";
    private final Time dateTime;
    private final ElementUtils elementUtils;
    private final ObjectMapper objectMapper;
    private final ManageOrderService manageOrderService;
    private final DgsService dgsService;
    private final UserService userService;
    private final DocumentLanguageService documentLanguageService;
    private final LocationRefDataService locationRefDataService;
    private final PartiesListGenerator partiesListGenerator;
    private final DynamicMultiSelectListService dynamicMultiSelectListService;
    private final HearingDataService hearingDataService;
    private final HearingService hearingService;

    private static final String DRAFT_ORDER_COLLECTION = "draftOrderCollection";
    private static final String ORDER_NAME = "orderName";
    private static final String MANAGE_ORDER_SDO_FAILURE
        = "Failed to update SDO order details";
    private static final String CASE_TYPE_OF_APPLICATION = "caseTypeOfApplication";
    private static final String IS_HEARING_PAGE_NEEDED = "isHearingPageNeeded";
    private static final String IS_ORDER_CREATED_BY_SOLICITOR = "isOrderCreatedBySolicitor";
    private static final String BOLD_BEGIN = "<span class='heading-h3'>";
    private static final String BOLD_END = "</span>";


    public Map<String, Object> getReturnedOrdersDynamicList(CaseData caseData) {

        Map<String, Object> caseDataMap = new HashMap<>();
        List<Element<DraftOrder>> supportedDraftOrderList = new ArrayList<>();
        caseData.getDraftOrderCollection().forEach(
            draftOrderElement -> {
                if (OrderStatusEnum.rejectedByJudge.getDisplayedValue()
                    .equalsIgnoreCase(draftOrderElement.getValue().getOtherDetails().getStatus())) {
                    supportedDraftOrderList.add(draftOrderElement);
                }
            }
        );
        caseDataMap.put("rejectedOrdersDynamicList", ElementUtils.asDynamicList(
            supportedDraftOrderList,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));
        log.info("*** draftoo dynamic list : {}", caseDataMap.get("rejectedOrdersDynamicList"));
        log.info("*** draftoo order collection : {}", caseData.getDraftOrderCollection());
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        return caseDataMap;
    }

}
