package uk.gov.hmcts.reform.prl.models.dto.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnlineCardPaymentRequest {
    @JsonProperty("amount")
    private BigDecimal amount;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("language")
    private String language;
    @JsonProperty("return-url")
    private String returnUrl;
}
