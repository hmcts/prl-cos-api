package uk.gov.hmcts.reform.prl.services.documentremoval.postabouttosubmitaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_ORDER_COLLECTION;

@Service
@Slf4j
public class DraftOrderUpdater implements DocumentRemovalAboutToSubmitAction {

    @Override
    public void onAboutToSubmit(CaseData caseData, Map<String, Object> caseDataUpdated) {
        List<Element<DraftOrder>> draftOrderCollection = ofNullable(caseData.getDraftOrderCollection())
            .orElse(new ArrayList<>());
        int draftOrderCount = draftOrderCollection.size();

        // Remove the draft order element if both the English and Welsh documents are no longer present
        draftOrderCollection.removeIf(draftOrderElement -> draftOrderElement.getValue().getOrderDocument() == null
            && draftOrderElement.getValue().getOrderDocumentWelsh() == null);

        if (draftOrderCount != draftOrderCollection.size()) {
            log.info("Case ID {}: Draft order collection size before {} size after {}", caseData.getId(),
                     draftOrderCount, draftOrderCollection.size());
            caseDataUpdated.put(DRAFT_ORDER_COLLECTION, draftOrderCollection);
        }
    }
}
