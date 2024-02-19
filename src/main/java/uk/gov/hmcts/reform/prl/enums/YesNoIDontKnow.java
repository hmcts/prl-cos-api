package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum YesNoIDontKnow {

    @JsonProperty("yes")
    yes("yes", "Yes"),
    @JsonProperty("no")
    no("no", "No"),
    @JsonProperty("dontKnow")
    dontKnow("dontKnow", "I don't know");


    private final String id;
    private final String displayedValue;


    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static YesNoIDontKnow getValue(String key) {
        return YesNoIDontKnow.valueOf(key);
    }

    public static YesNoIDontKnow getDisplayedValueIgnoreCase(String enteredValue) {
        if (enteredValue.equalsIgnoreCase("Yes")) {
            return YesNoIDontKnow.yes;
        } else if (enteredValue.equalsIgnoreCase("No")) {
            return YesNoIDontKnow.no;
        } else if (enteredValue.equalsIgnoreCase("dontKnow")) {
            return YesNoIDontKnow.dontKnow;
        } else {
            return null;
        }
    }
}
