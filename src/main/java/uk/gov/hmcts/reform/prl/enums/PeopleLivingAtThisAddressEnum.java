package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

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
        if (enteredValue.equalsIgnoreCase("applicant")) {
            return PeopleLivingAtThisAddressEnum.applicant;
        } else if (enteredValue.equalsIgnoreCase("respondent")) {
            return PeopleLivingAtThisAddressEnum.respondent;
        } else if (enteredValue.equalsIgnoreCase("children")) {
            return PeopleLivingAtThisAddressEnum.applicantChildren;
        } else if (enteredValue.equalsIgnoreCase("other")) {
            return PeopleLivingAtThisAddressEnum.someoneElse;
        } else {
            return null;
        }
    }
}
