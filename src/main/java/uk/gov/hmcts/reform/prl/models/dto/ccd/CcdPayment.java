package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CcdPayment {
    private String paymentAmount;
    private String paymentReference;
    private String paymentMethod;
    private String caseReference;
    private String accountNumber;
}
