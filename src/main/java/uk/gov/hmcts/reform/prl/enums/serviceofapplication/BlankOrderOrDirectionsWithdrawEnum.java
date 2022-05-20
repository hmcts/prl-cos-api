package uk.gov.hmcts.reform.prl.enums.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum BlankOrderOrDirectionsWithdrawEnum {

    blankOrderOrDirectionsWithdraw("Blank order or directions (C21) - to withdraw application");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static BlankOrderOrDirectionsWithdrawEnum getValue(String key) {
        return BlankOrderOrDirectionsWithdrawEnum.valueOf(key);
    }

}
