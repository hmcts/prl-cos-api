package uk.gov.hmcts.reform.prl.models.dto.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeeDto {
    @JsonProperty("calculated_amount")
    private BigDecimal calculatedAmount;
    @JsonProperty("code")
    private String code;
    @JsonProperty("version")
    private Integer version;
    @JsonProperty("volume")
    private Integer volume;
}
