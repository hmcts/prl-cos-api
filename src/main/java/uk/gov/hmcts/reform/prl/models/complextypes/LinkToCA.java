package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder
public class LinkToCA {

    @JsonProperty("linkToCAApplication")
    private final YesOrNo linkToCAApplication;
    @JsonProperty("childArrangementsApplicationNumber")
    private final String childArrangementsApplicationNumber;
}
