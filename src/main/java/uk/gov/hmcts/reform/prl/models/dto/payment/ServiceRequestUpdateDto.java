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
public class ServiceRequestUpdateDto {
    @JsonProperty("service_request_reference")
    private String serviceRequestReference;
    @JsonProperty("ccd_case_number")
    private String ccdCaseNumber;
    @JsonProperty("service_request_amount")
    private BigDecimal serviceRequestAmount;
    @JsonProperty("service_request_status")
    private String serviceRequestStatus;
    @JsonProperty("payment")
    private PaymentDto payment;
}
