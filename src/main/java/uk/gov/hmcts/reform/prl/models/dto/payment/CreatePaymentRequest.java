package uk.gov.hmcts.reform.prl.models.dto.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePaymentRequest {
    String caseId;
    String returnUrl;
    String applicantCaseName;
    String hwfRefNumber;
}
