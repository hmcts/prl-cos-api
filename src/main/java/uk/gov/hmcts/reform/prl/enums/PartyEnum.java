package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartyEnum {

    @JsonProperty("applicant")
    Applicant("Applicant"),
    @JsonProperty("respondent")
    Respondent("Respondent"),
    @JsonProperty("other")
    Other("Other people in the case");

    private final String displayedValue;

}
