package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ApplicantOrChildren {

    @JsonProperty("applicants")
    APPLICANTS("applicants", "Applicant(s)"),
    @JsonProperty("children")
    CHILDREN("children", "Child(ren)");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ApplicantOrChildren getValue(String key) {
        return ApplicantOrChildren.valueOf(key);
    }

}
