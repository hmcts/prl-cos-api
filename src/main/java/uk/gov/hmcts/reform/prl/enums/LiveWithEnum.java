package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LiveWithEnum {

    @JsonProperty("applicant")
    APPLICANT("applicant", "Applicant"),
    @JsonProperty("respondent")
    RESPONDENT("respondent", "Respondent"),
    @JsonProperty("anotherPerson")
    ANOTHER_PERSON("anotherPerson", "Another person not listed");

    private final String id;
    private final String displayedValue;

}
