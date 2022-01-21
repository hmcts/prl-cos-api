package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum YesOrNo {

    @JsonProperty("yes")
    yes("Yes"),

    @JsonProperty("yes")
    no("No");

    private final String value;

    @JsonCreator
    public static YesOrNo getValue(String key) {
        return YesOrNo.valueOf(key);
    }

    @JsonIgnore
    public boolean toBoolean() {
        return yes.name().equalsIgnoreCase(this.name());
    }
}
