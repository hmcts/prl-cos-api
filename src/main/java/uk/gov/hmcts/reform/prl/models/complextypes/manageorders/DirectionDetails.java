package uk.gov.hmcts.reform.prl.models.complextypes.manageorders;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.BoldEnum;

import java.util.List;

@Builder(toBuilder = true)
@Data
public class DirectionDetails {

    @JsonProperty("isBold")
    private final List<BoldEnum> isBold;
    @JsonProperty("directionText")
    private final String directionText;

}
