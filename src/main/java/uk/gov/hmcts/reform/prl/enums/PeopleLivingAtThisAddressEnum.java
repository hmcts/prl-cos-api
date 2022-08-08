package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum PeopleLivingAtThisAddressEnum {

    @JsonProperty("applicant")
    applicant("applicant", "The applicant"),

    @JsonProperty("respondent")
    respondent("respondent", "The respondent"),

    @JsonProperty("applicantChildren")
    applicantChildren("applicantChildren", "The applicant’s child or children"),

    @JsonProperty("someoneElse")
    someoneElse("someoneElse", "Someone else - please specify");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static PeopleLivingAtThisAddressEnum getValue(String key) {
        return PeopleLivingAtThisAddressEnum.valueOf(key);
    }

    public static PeopleLivingAtThisAddressEnum getDisplayedValueFromEnumString(String enteredValue) {
        return Arrays.stream(PeopleLivingAtThisAddressEnum.values())
            .map(i -> PeopleLivingAtThisAddressEnum.valueOf(enteredValue))
            .findFirst().orElse(null);
    }
}
