package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WhoChildrenLiveWith {

    applicant("Applicant(s)"),
    respondent("Respondent(s)"),
    other("Other");

    private final String displayedValue;

}
