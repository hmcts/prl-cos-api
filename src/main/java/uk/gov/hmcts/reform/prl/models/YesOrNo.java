package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum YesOrNo {

    @JsonProperty("Yes")
    YES("Yes"),

    @JsonProperty("No")
    NO("No");

    private final String value;

    public static YesOrNo from(Boolean val) {
        return val ? YES : NO;
    }

    @JsonIgnore
    public boolean toBoolean() {
        return YES.name().equalsIgnoreCase(this.name());
    }
}
