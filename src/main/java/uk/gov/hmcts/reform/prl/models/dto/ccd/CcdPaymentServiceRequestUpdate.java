package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CcdPaymentServiceRequestUpdate {

    private String serviceRequestReference;
    private String ccdCaseNumber;
    private String serviceRequestAmount;
    private String serviceRequestStatus;
    private LocalDateTime callBackUpdateTimestamp;
    private CcdPayment payment;

}
