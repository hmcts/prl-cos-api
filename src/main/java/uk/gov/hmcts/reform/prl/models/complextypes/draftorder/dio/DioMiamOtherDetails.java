package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class DioMiamOtherDetails {

    @JsonProperty("dioMiamOtherDetails")
    private final String dioMiamOtherDetails;

    @JsonCreator
    public DioMiamOtherDetails(String dioMiamOtherDetails) {
        this.dioMiamOtherDetails  = dioMiamOtherDetails;
    }
}
