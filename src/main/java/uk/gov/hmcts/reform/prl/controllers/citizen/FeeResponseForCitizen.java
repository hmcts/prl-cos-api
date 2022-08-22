package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class FeeResponseForCitizen {
    @JsonProperty(value = "feeAmountForC100Application")
    private BigDecimal amount;

    @JsonProperty(value = "errorRetrievingResponse")
    private String errorRetrievingResponse;
}

