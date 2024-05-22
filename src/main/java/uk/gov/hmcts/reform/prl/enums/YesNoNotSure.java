package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum YesNoNotSure {

    @JsonProperty("yes")
    yes("yes", "Yes"),
    @JsonProperty("no")
    no("no", "No"),
    @JsonProperty("notSure")
    notSure("notSure", "I am not sure");


    private final String id;
    private final String displayedValue;


    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static YesNoNotSure getValue(String key) {
        return YesNoNotSure.valueOf(key);
    }

    public static YesNoNotSure getDisplayedValueIgnoreCase(String enteredValue) {
        if (enteredValue.equalsIgnoreCase("Yes")) {
            return YesNoNotSure.yes;
        } else if (enteredValue.equalsIgnoreCase("No")) {
            return YesNoNotSure.no;
        } else if (enteredValue.equalsIgnoreCase("I am not sure")) {
            return YesNoNotSure.notSure;
        } else {
            return null;
        }
    }
}
