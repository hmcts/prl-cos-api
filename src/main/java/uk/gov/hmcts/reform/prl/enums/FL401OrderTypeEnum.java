package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum FL401OrderTypeEnum {

    @CCD(label = "Non-molestation order")
    @JsonProperty("nonMolestationOrder")
    nonMolestationOrder("Non-molestation order"),

    @CCD(label = "Occupation order")
    @JsonProperty("occupationOrder")
    occupationOrder("Occupation order");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static FL401OrderTypeEnum getValue(String key) {
        return FL401OrderTypeEnum.valueOf(key);
    }
}
