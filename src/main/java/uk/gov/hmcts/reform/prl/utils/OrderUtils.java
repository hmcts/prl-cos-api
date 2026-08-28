package uk.gov.hmcts.reform.prl.utils;

import org.apache.commons.collections.CollectionUtils;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_COLLECTION;

public class OrderUtils {


    @SuppressWarnings("unchecked")
    public static UUID getOrderId(Map<String, Object> caseDataUpdated) {
        UUID orderCollectionId = null;
        if (caseDataUpdated.containsKey(ORDER_COLLECTION) && null != caseDataUpdated.get(ORDER_COLLECTION)) {
            List<Element<OrderDetails>> orderCollection = (List<Element<OrderDetails>>) caseDataUpdated.get(ORDER_COLLECTION);
            orderCollectionId = CollectionUtils.isNotEmpty(orderCollection) ? orderCollection.getFirst().getId() : null;
        }
        return orderCollectionId;
    }


    @SuppressWarnings("unchecked")
    public static  UUID getDraftOrderId(String authorisation, Map<String, Object> caseDataUpdated, String loggedInUserType) {
        UUID newDraftOrderCollectionId = null;
        if ((UserRoles.COURT_ADMIN.name().equals(loggedInUserType) || UserRoles.JUDGE.name().equals(loggedInUserType))
            && caseDataUpdated.containsKey(DRAFT_ORDER_COLLECTION)
            && null != caseDataUpdated.get(DRAFT_ORDER_COLLECTION)) {

            var draftOrderCollection = (List) caseDataUpdated.get(DRAFT_ORDER_COLLECTION);
            if (CollectionUtils.isNotEmpty(draftOrderCollection)) {
                Object first = draftOrderCollection.getFirst();
                if (first instanceof Map<?, ?>) {
                    List<Map<String, Object>> orders = (List<Map<String, Object>>) draftOrderCollection;
                    newDraftOrderCollectionId = CollectionUtils.isNotEmpty(draftOrderCollection)
                        ? UUID.fromString((String)orders.getFirst().get("id")) : null;
                }
                if (first instanceof  Element<?>) {
                    List<Element<DraftOrder>> orders = (List<Element<DraftOrder>>) draftOrderCollection;
                    newDraftOrderCollectionId = orders.getFirst().getId();
                }
            }
        }
        return newDraftOrderCollectionId;
    }
}
