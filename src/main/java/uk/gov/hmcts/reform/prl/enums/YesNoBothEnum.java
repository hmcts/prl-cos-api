package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum YesNoBothEnum {

    @JsonProperty("yesBothOfThem")
    yesBothOfThem("Yes, both of them"),

    @JsonProperty("yesApplicant")
    yesApplicant("Yes, the applicant"),

    @JsonProperty("yesRespondent")
    yesRespondent("Yes, the respondent"),

    @JsonProperty("No")
    No("No");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static YesNoBothEnum getValue(String key) {
        return YesNoBothEnum.valueOf(key);
    }

    public static YesNoBothEnum getDisplayedValueFromEnumString(String enteredValue) {
        if (enteredValue.equalsIgnoreCase("applicantAndRespondent")) {
            return YesNoBothEnum.yesBothOfThem;
        } else if (enteredValue.equalsIgnoreCase("applicant")) {
            return YesNoBothEnum.yesApplicant;
        } else if (enteredValue.equalsIgnoreCase("respondent")) {
            return YesNoBothEnum.yesRespondent;
        } else if (enteredValue.equalsIgnoreCase("neither")) {
            return YesNoBothEnum.No;
        } else {
            return null;
        }
    }
}
