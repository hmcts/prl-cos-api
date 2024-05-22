package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum TransferToAnotherCourtReasonEnum {

    @JsonProperty("anotherJurisdiction")
    anotherJurisdiction("anotherJurisdiction", "The child lives in another jurisdiction."),

    @JsonProperty("anotherReason")
    anotherReason("anotherReason", "There is another reason for proceedings to be transferred.");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static TransferToAnotherCourtReasonEnum getValue(String key) {
        return TransferToAnotherCourtReasonEnum.valueOf(key);
    }
}
