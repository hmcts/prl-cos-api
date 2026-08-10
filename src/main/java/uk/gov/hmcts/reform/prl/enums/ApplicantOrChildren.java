package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "applicantOrChildren", generate = true)
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ApplicantOrChildren {

    @CCD(label = "Applicant(s)")
    @JsonProperty("applicants")
    applicants("applicants", "Applicant(s)"),
    @CCD(label = "Child(ren)")
    @JsonProperty("children")
    children("children", "Child(ren)");


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
