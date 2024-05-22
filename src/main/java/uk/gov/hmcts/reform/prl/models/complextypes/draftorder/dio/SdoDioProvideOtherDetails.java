package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class SdoDioProvideOtherDetails {

    @JsonProperty("sdoDioOtherDetails")
    private final String sdoDioOtherDetails;

    @JsonCreator
    public SdoDioProvideOtherDetails(String sdoDioOtherDetails) {
        this.sdoDioOtherDetails  = sdoDioOtherDetails;
    }
}
