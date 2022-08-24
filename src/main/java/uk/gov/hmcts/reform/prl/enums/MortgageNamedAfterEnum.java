package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MortgageNamedAfterEnum {

    @JsonProperty("applicant")
    applicant("applicant", "The applicant"),

    @JsonProperty("respondent")
    respondent("respondent", "The respondent"),

    @JsonProperty("someoneElse")
    someoneElse("someoneElse", "Someone else - please specify");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static MortgageNamedAfterEnum getValue(String key) {
        return MortgageNamedAfterEnum.valueOf(key);
    }

    public static MortgageNamedAfterEnum getDisplayedValueFromEnumString(String enteredValue) {
        return Arrays.stream(MortgageNamedAfterEnum.values())
            .map(i -> MortgageNamedAfterEnum.valueOf(enteredValue))
            .findFirst().orElse(null);
    }
}
