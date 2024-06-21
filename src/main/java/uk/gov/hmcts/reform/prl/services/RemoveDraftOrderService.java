package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"java:S3776", "java:S6204"})
public class RemoveDraftOrderService {

    private static final String REMOVE_DRAFT_ORDERS_DYNAMIC_LIST = "removeDraftOrdersDynamicList";

    private final ElementUtils elementUtils;
    private final ObjectMapper objectMapper;
    private static final String CASE_TYPE_OF_APPLICATION = "caseTypeOfApplication";

    public Map<String, Object> getDraftOrderDynamicList(CaseData caseData,
                                                        String eventId,
                                                        String authorisation) {

        Map<String, Object> caseDataMap = new HashMap<>();
        List<Element<DraftOrder>> supportedDraftOrderList = new ArrayList<>();
        caseData.getDraftOrderCollection().forEach(
            draftOrderElement -> {
                if (Event.REMOVE_DRAFT_ORDER.getId().equalsIgnoreCase(eventId)) {
                    supportedDraftOrderList.add(draftOrderElement);
                }
            }
        );
        caseDataMap.put(REMOVE_DRAFT_ORDERS_DYNAMIC_LIST, ElementUtils.asDynamicList(
            supportedDraftOrderList,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));

        return caseDataMap;
    }

    public List<Element<DraftOrder>> removeSelectedDraftOrder(CaseData caseData) {

        if (ObjectUtils.isNotEmpty(caseData.getSendAndReplyDto().getRemoveDraftOrdersDynamicList())
            && ObjectUtils.isNotEmpty(caseData.getDraftOrderCollection())) {

            UUID orderId = elementUtils.getDynamicListSelectedValue(caseData.getSendAndReplyDto().getRemoveDraftOrdersDynamicList(), objectMapper);
            return caseData.getDraftOrderCollection().stream()
                .filter(element -> !element.getId().equals(orderId)).collect(Collectors.toList());

        }
        return caseData.getDraftOrderCollection();
    }
}
