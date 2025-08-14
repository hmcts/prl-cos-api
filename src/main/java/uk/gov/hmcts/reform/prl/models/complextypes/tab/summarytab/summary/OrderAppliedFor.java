package uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OrderAppliedFor {

    private final String ordersApplyingFor;
    private final String typeOfChildArrangementsOrder;

}
