package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum YesNoDontKnow {

    @CCD(label = "Yes")
    @JsonProperty("yes")
    yes("yes", "Yes"),
    @CCD(label = "No")
    @JsonProperty("no")
    no("no", "No"),
    @CCD(label = "Don't know")
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
