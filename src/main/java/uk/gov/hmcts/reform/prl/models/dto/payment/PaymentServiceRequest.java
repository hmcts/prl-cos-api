package uk.gov.hmcts.reform.prl.models.dto.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentServiceRequest {
    @JsonProperty("call_back_url")
    private String callBackUrl;
    @JsonProperty("case_payment_request")
    private CasePaymentRequestDto casePaymentRequest;
    @JsonProperty("case_reference")
    private String caseReference;
    @JsonProperty("ccd_case_number")
    private String ccdCaseNumber;
    @JsonProperty("fees")
    private FeeDto[] fees;
    @Builder.Default
    @JsonProperty("hmcts_org_id")
    private String hmctsOrgId = "ABA5";

}
