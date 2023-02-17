package uk.gov.hmcts.reform.prl.models.dto.payment;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentStatusResponse {

    @JsonProperty(value = "amount")
    private String amount;
    @JsonProperty(value = "reference")
    private String reference;
    @JsonProperty(value = "ccd_case_number")
    private String ccdcaseNumber;
    @JsonProperty(value = "case_reference")
    private String caseReference;
    @JsonProperty(value = "channel")
    private String channel;
    @JsonProperty(value = "method")
    private String method;
    @JsonProperty(value = "status")
    private String status;
    @JsonProperty(value = "external_reference")
    private String externalReference;
    @JsonProperty(value = "payment_group_reference")
    private String paymentGroupReference;
}
