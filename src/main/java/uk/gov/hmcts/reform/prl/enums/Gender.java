package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum Gender {

    @JsonProperty("female")
    female("female", "Female"),
    @JsonProperty("male")
    male("male", "Male"),
    @JsonProperty("other")
    other("other", "They identify in another way");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static Gender getValue(String key) {
        return Gender.valueOf(key);
    }

    public static Gender getDisplayedValueFromEnumString(String enteredValue) {
        if (enteredValue.equalsIgnoreCase("Female")) {
            return Gender.female;
        } else if (enteredValue.equalsIgnoreCase("Male")) {
            return Gender.male;
        } else if ((enteredValue.equalsIgnoreCase("Non-binary"))
            || (enteredValue.equalsIgnoreCase("Transgender"))
            || (enteredValue.equalsIgnoreCase("other"))) {
            return Gender.other;
        }
        return null;
    }
}
