package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LiveWithEnum {

    @JsonProperty("applicant")
    applicant("applicant", "Applicant"),
    @JsonProperty("respondent")
    respondent("respondent", "Respondent"),
    @JsonProperty("anotherPerson")
    anotherPerson("anotherPerson", "Another person not listed");

    private final String id;
    private final String displayedValue;
}
