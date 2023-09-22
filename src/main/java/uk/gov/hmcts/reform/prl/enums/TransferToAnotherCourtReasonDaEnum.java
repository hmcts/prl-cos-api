package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum TransferToAnotherCourtReasonDaEnum {

    @JsonProperty("anotherJurisdiction")
    anotherJurisdiction("anotherJurisdiction", "A party lives in a different court's jurisdiction."),

    @JsonProperty("anotherReason")
    anotherReason("anotherReason", "There is another reason for proceedings to be transferred.");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static TransferToAnotherCourtReasonDaEnum getValue(String key) {
        return TransferToAnotherCourtReasonDaEnum.valueOf(key);
    }
}
