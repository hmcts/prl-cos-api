package uk.gov.hmcts.reform.prl.enums.ServiceOfApplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum TransferOfCaseToAnotherCourtEnum {

    transferOfCaseToAnotherCourt("Transfer of case to another court (C49)");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static TransferOfCaseToAnotherCourtEnum getValue(String key) {
        return TransferOfCaseToAnotherCourtEnum.valueOf(key);
    }
}
