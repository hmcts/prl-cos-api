package uk.gov.hmcts.reform.prl.models.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CitizenAwpPayment {

    private String awpType;
    private String partType;
    private String feeType;
    private String fee;
    private String serviceReqRef;
    private String paymentReqRef;

}
