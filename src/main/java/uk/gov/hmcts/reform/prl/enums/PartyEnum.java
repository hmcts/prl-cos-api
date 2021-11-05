package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartyEnum {

    Applicant("Applicant"),
    Respondent("Respondent"),
    Other("Someone else");

    private final String displayedValue;

}
