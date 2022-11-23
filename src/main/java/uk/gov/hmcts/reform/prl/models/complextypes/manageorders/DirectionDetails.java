package uk.gov.hmcts.reform.prl.models.complextypes.manageorders;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder(toBuilder = true)
@Data
public class DirectionDetails {

    @JsonProperty("isBold")
    private final List<String> isBold;
    @JsonProperty("directionText")
    private final String directionText;

}
