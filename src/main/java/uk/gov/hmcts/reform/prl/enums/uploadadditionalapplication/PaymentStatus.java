package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
@Getter
public enum PaymentStatus {

    PENDING("Pending"),
    NOT_APPLICABLE("Not applicable"),
    PAID("Paid"),
    HWF("Help with fees");

    private final String displayedValue;

    @JsonCreator
    public static PaymentStatus getValue(String key) {
        return PaymentStatus.valueOf(key);
    }

}
