package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MortgageNamedAfterEnum {

    @CCD(label = "The applicant")
    @JsonProperty("applicant")
    applicant("applicant", "The applicant"),

    @CCD(label = "The respondent")
    @JsonProperty("respondent")
    respondent("respondent", "The respondent"),

    @CCD(label = "Someone else - please specify")
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
        if (enteredValue.equalsIgnoreCase("applicant")) {
            return MortgageNamedAfterEnum.applicant;
        } else if (enteredValue.equalsIgnoreCase("respondent")) {
            return MortgageNamedAfterEnum.respondent;
        } else if (enteredValue.equalsIgnoreCase("other")) {
            return MortgageNamedAfterEnum.someoneElse;
        } else {
            return null;
        }
    }
}
