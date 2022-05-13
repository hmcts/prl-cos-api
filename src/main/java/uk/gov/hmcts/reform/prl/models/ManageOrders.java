package uk.gov.hmcts.reform.prl.models;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;

import java.util.List;

@Data
@Builder
@Jacksonized
public class ManageOrders {
    private final List<OrderTypeEnum> childArrangementsOrdersToIssue;
    private final ChildArrangementOrderTypeEnum selectChildArrangementsOrder;
    private final List<String> cafcassEmailAddress;
    private final List<String> otherEmailAddress;

    private final String recitalsOrPreamble;
    private final String orderDirections;
    private final String furtherDirectionsIfRequired;
    private final JudgeOrMagistrateTitleEnum judgeOrMagistrateTitle;
    private final String justiceLegalAdviserFullName;

}
