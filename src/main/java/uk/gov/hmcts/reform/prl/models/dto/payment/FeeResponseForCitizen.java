package uk.gov.hmcts.reform.prl.models.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class FeeResponseForCitizen {
    @JsonProperty(value = "feeAmount")
    private String amount;

    @JsonProperty(value = "feeType")
    private String feeType;

    @JsonProperty(value = "errorRetrievingResponse")
    private String errorRetrievingResponse;
}
