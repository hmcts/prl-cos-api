package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;

import java.util.List;

@Builder
@Data
public class TypeOfApplication {

    private final String ordersApplyingFor;
    private final String typeOfChildArrangementsOrder;
    private final String natureOfOrder;

}
