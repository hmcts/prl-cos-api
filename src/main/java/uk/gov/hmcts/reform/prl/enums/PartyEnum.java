package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartyEnum {

    @JsonProperty("applicant")
    applicant("Applicant"),
    @JsonProperty("respondent")
    respondent("Respondent"),
    @JsonProperty("other")
    other("Someone else");

    private final String displayedValue;

}
