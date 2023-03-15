package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ContactPreferences {

    @JsonProperty("digital")
    digital("digital", "digital"),
    @JsonProperty("post")
    post("post", "post");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ContactPreferences getValue(String key) {
        return ContactPreferences.valueOf(key);
    }

    public static ContactPreferences fromValue(String value) {
        return Arrays.stream(values())
            .filter(contactPref -> contactPref.getDisplayedValue().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown contact preference: " + value));
    }
}
