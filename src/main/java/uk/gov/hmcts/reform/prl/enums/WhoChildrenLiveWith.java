package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum WhoChildrenLiveWith {

    applicant("Applicant(s)"),
    respondent("Respondent(s)"),
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
