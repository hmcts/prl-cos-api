package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum YesOrNo {

    @JsonProperty("yes")
    yes("Yes"),

    @JsonProperty("no")
    no("No");

    private final String value;

    @JsonCreator
    public static YesOrNo getValue(String key) {
        return YesOrNo.valueOf(key);
    }

    @JsonValue
    public String getDisplayedValue() {
        return value;
    }

    @JsonIgnore
    public boolean toBoolean() {
        return yes.name().equalsIgnoreCase(this.name());
    }
}
