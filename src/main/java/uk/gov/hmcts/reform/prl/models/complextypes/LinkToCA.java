package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder
@Jacksonized
public class LinkToCA {

    @JsonProperty("linkToCAApplication")
    private final YesOrNo linkToCaApplication;
    @JsonProperty("childArrangementsApplicationNumber")
    private final String childArrangementsApplicationNumber;
}
