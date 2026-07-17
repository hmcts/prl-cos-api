package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;


@Data
@Builder
@Jacksonized
public class TypeOfApplicationOrders {

    @CCD(label = "*Which order(s) are you applying for?", searchable = false)
    private final List<FL401OrderTypeEnum> orderType;
}
