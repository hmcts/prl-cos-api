package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.prl.enums.Event.MANAGE_ORDERS;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MiamForOrderService {

    private final DraftAnOrderService draftAnOrderService;
    private final ObjectMapper objectMapper;

    public Map<String, Object> updateCaseDataWithMiamForOrderDetails(CaseDetails caseDetails, String eventId, String clientContext, Map<String, Object> response){

        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        YesOrNo eligibleStateForMiam = obtainEligibleStateForMiam(caseData);
        DraftOrder selectedOrder = null;
        if (!MANAGE_ORDERS.getId().equalsIgnoreCase(eventId)) {
            selectedOrder = draftAnOrderService.getSelectedDraftOrderDetails(
                caseData.getDraftOrderCollection(),
                caseData.getDraftOrdersDynamicList(),
                clientContext,
                eventId
            );
        }

        if (nonNull(selectedOrder)) {
            response.put("miamForOrder", selectedOrder.getMiamForOrder());
            response.put("orderType", selectedOrder.getOrderType());
        }
        response.put("eligibleStateForMiam", eligibleStateForMiam);
        return response;
    }

    private YesOrNo obtainEligibleStateForMiam(CaseData caseData) {
        State state = caseData.getState();
        YesOrNo eligibleStateForMiam = null;
        if (nonNull(state)) {
            String status = state.getValue();
            if (status.equalsIgnoreCase(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue())
                ||  status.equalsIgnoreCase(State.DECISION_OUTCOME.getValue())) {
                eligibleStateForMiam = Yes;
            } else {
                eligibleStateForMiam = No;
            }

        }
        return eligibleStateForMiam;
    }
}
