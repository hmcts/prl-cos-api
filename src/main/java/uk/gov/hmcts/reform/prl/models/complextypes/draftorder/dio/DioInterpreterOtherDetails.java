package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class DioInterpreterOtherDetails {
    @JsonProperty("dioInterpreterOtherDetails")
    private final String dioInterpreterOtherDetails;

    @JsonCreator
    public DioInterpreterOtherDetails(String dioInterpreterOtherDetails) {
        this.dioInterpreterOtherDetails  = dioInterpreterOtherDetails;
    }
}
