package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Proceedings {
    private final TypeOfOrderEnum orderType;
    @JsonProperty("proceedingDetails")
    private final List<Element<OtherProceedingDetails>> proceedingDetails;
}
