package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder
@Jacksonized
public class LinkToCA {

    @JsonProperty("linkToCaApplication")
    private final YesOrNo linkToCaApplication;
    @JsonProperty("cAApplicationNumber")
    private final String cAApplicationNumber;

    @JsonCreator
    public LinkToCA(YesOrNo linkToCaApplication, String childArrangementsApplicationNumber) {
        this.linkToCaApplication = linkToCaApplication;
        this.cAApplicationNumber = childArrangementsApplicationNumber;
    }
}
