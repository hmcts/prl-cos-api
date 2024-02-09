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
public class PaymentResponse {

    @JsonProperty(value = "payment_reference")
    private String paymentReference;
    @JsonProperty(value = "date_created")
    private String dateCreated;
    @JsonProperty(value = "external_reference")
    private String externalReference;
    @JsonProperty(value = "next_url")
    private String nextUrl;
    @JsonProperty(value = "status")
    private String paymentStatus;
    private String serviceRequestReference;
    private String applicantCaseName;
}

