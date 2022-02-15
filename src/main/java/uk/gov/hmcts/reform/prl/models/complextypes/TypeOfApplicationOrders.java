package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;

import java.util.List;


@Data
@Builder
@Jacksonized
public class TypeOfApplicationOrders {

    private final List<FL401OrderTypeEnum> orderType;
}
