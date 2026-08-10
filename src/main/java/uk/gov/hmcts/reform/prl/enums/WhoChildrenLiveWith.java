package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum WhoChildrenLiveWith {

    @CCD(label = "Applicant(s)")
    applicant("Applicant(s)"),
    @CCD(label = "Respondent(s)")
    respondent("Respondent(s)"),
    @CCD(label = "Other")
    other("Other");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static WhoChildrenLiveWith getValue(String key) {
        return WhoChildrenLiveWith.valueOf(key);
    }

}
