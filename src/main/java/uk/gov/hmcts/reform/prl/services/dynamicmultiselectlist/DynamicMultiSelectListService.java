package uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DynamicMultiSelectListService {

    public DynamicMultiSelectList getOrdersAsDynamicMultiSelectList(CaseData caseData) {
        List<Element<OrderDetails>> orders = caseData.getOrderCollection();
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        orders.forEach(order -> {
            OrderDetails orderDetails = order.getValue();
            listItems.add(DynamicMultiselectListElement.builder().code(orderDetails.getOrderTypeId())
                              .label(orderDetails.getLabelForDynamicList()).build());
        });
        return DynamicMultiSelectList.builder().listItems(listItems).build();
    }
}
