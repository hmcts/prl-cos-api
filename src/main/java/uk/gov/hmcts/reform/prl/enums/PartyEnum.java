package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum PartyEnum {

    @JsonProperty("applicant")
    applicant("Applicant"),
    @JsonProperty("applicant_solicitor")
    applicant_solicitor("Applicant solicitor"),
    @JsonProperty("respondent")
    respondent("Respondent"),
    @JsonProperty("respondent_solicitor")
    respondent_solicitor("Respondent solicitor"),
    @JsonProperty("other")
    other("Other people in the case");

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
