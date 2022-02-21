package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class OtherDetailsOfWithoutNoticeOrder {
    @JsonProperty("otherDetails")
    private final String otherDetails;

    @JsonCreator
    public OtherDetailsOfWithoutNoticeOrder(String otherDetails) {
        this.otherDetails = otherDetails;
    }
}
