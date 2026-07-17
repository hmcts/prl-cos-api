package uk.gov.hmcts.reform.prl.models.dto.payment;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
public class CitizenAwpPayment {

    @CCD(label = "Awp type", searchable = false)
    private String awpType;
    @CCD(label = "Party type", searchable = false)
    private String partType;
    @CCD(label = "Fee type", searchable = false)
    private String feeType;
    @CCD(label = "Fee", searchable = false)
    private String fee;
    @CCD(label = "Service request ref", searchable = false)
    private String serviceReqRef;
    @CCD(label = "Payment request ref", searchable = false)
    private String paymentReqRef;

}
