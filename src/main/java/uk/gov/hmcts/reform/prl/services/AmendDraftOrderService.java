package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"java:S3776", "java:S6204"})
public class AmendDraftOrderService {

    public static final String DRAFT_ORDERS_DYNAMIC_LIST = "draftOrdersDynamicList";

    private final ManageOrderService manageOrderService;

    private static final String CASE_TYPE_OF_APPLICATION = "caseTypeOfApplication";


    public Map<String, Object> getDraftOrderDynamicList(CaseData caseData,
                                                        String eventId,
                                                        String authorisation) {
        String loggedInUserType = manageOrderService.getLoggedInUserType(authorisation);
        Map<String, Object> caseDataMap = new HashMap<>();
        List<Element<DraftOrder>> supportedDraftOrderList = new ArrayList<>();
        caseData.getDraftOrderCollection().forEach(
            draftOrderElement -> {
                log.info(" ---> eventId {}", eventId);
                if (Event.AMEND_DRAFT_ORDER.getId().equalsIgnoreCase(eventId)) {
                    supportedDraftOrderList.add(draftOrderElement);
                }
            }
        );
        caseDataMap.put(DRAFT_ORDERS_DYNAMIC_LIST, ElementUtils.asDynamicList(
            supportedDraftOrderList,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));

        return caseDataMap;
    }
}
