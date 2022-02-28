package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicantOrChildren {

    @JsonProperty("applicants")
    applicants("applicants", "Applicant(s)"),
    @JsonProperty("children")
    children("children", "Child(ren)");


    private final String id;
    private final String displayedValue;


}
