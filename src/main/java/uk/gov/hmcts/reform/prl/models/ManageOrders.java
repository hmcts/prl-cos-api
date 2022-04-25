package uk.gov.hmcts.reform.prl.models;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.util.List;

@Data
@Builder
@Jacksonized
public class ManageOrders {
    private final List<OrderTypeEnum> childArrangementsOrdersToIssue;
    private final ChildArrangementOrderTypeEnum selectChildArrangementsOrder;
    private final List<String> cafcassEmailAddress;
    private final List<String> otherEmailAddress;

}
