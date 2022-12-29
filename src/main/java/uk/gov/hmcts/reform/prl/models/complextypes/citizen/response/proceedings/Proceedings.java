package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class Proceedings {
    private final TypeOfOrderEnum orderType;
    private final List<OtherProceedingDetails> proceedingDetails;
}
