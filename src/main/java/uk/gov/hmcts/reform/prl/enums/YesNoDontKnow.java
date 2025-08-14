package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum YesNoDontKnow {

    @JsonProperty("yes")
    yes("yes", "Yes"),
    @JsonProperty("no")
    no("no", "No"),
    @JsonProperty("dontKnow")
    dontKnow("dontKnow", "Don't know");


    private final String id;
    private final String displayedValue;


    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static YesNoDontKnow getValue(String key) {
        return YesNoDontKnow.valueOf(key);
    }

    public static YesNoDontKnow getDisplayedValueIgnoreCase(String enteredValue) {
        if (enteredValue.equalsIgnoreCase("Yes")) {
            return YesNoDontKnow.yes;
        } else if (enteredValue.equalsIgnoreCase("No")) {
            return YesNoDontKnow.no;
        } else if (enteredValue.equalsIgnoreCase("dontKnow")) {
            return YesNoDontKnow.dontKnow;
        } else {
            return null;
        }
    }
}
