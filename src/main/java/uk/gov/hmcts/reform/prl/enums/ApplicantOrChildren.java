package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicantOrChildren {

    APPLICANTS("applicant", "Applicant(s)"),
    CHILDREN("children", "Child(ren)");


    private final String id;
    private final String displayedValue;

}
