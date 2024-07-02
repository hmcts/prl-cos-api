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
public class ServiceRequestReferenceStatusResponse {

    @JsonProperty(value = "service_request_status")
    private String serviceRequestStatus;
    @JsonProperty(value = "payment_group_reference")
    private String serviceRequestReference;
}
