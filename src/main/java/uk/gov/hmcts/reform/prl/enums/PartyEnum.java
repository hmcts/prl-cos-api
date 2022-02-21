package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PartyEnum {

    @JsonProperty("applicant")
    applicant("Applicant"),
    @JsonProperty("respondent")
    respondent("Respondent"),
    @JsonProperty("other")
    other("Someone else");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static PartyEnum getValue(String key) {
        return PartyEnum.valueOf(key);
    }

}
