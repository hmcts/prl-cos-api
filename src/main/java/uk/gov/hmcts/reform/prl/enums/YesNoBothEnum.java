package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@RequiredArgsConstructor
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
}
