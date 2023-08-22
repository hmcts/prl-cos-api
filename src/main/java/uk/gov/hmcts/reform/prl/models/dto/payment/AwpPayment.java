package uk.gov.hmcts.reform.prl.models.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
@Builder(toBuilder = true)
public class AwpPayment {

    private String awpType;
    private String partType;
    private String feeType;
    private String serviceReqRef;
    private String paymentReqRef;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AwpPayment that = (AwpPayment) o;
        return Objects.equals(awpType, that.awpType)
            && Objects.equals(partType, that.partType)
            && Objects.equals(feeType, that.feeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(awpType, partType, feeType);
    }

}
