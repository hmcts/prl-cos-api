package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class DioTransferCourtDetails {
    @JsonProperty("dioTransferCourtDetails")
    private final String dioTransferCourtDetails;

    @JsonCreator
    public DioTransferCourtDetails(String dioTransferCourtDetails) {
        this.dioTransferCourtDetails  = dioTransferCourtDetails;
    }
}

