package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LiveWithEnum {

    APPLICANT("applicant", "Applicant"),
    RESPONDENT("respondent", "Respondent"),
    ANOTHER_PERSON("anotherPerson", "Another person not listed");

    private final String id;
    private final String displayedValue;

}
