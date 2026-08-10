package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "PaymentCallbackServiceRequestUpdate", generate = true)
@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CcdPaymentServiceRequestUpdate {

    @CCD(label = " ", searchable = false)
    private String serviceRequestReference;
    @CCD(label = " ", searchable = false)
    private String ccdCaseNumber;
    @CCD(label = " ", searchable = false)
    private String serviceRequestAmount;
    @CCD(label = " ", searchable = false)
    private String serviceRequestStatus;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime callBackUpdateTimestamp;
    @CCD(label = " ", searchable = false)
    private CcdPayment payment;

}
