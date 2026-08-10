package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum Gender {

    @CCD(label = "Female")
    @JsonProperty("female")
    female("female", "Female"),
    @CCD(label = "Male")
    @JsonProperty("male")
    male("male", "Male"),
    @CCD(label = "They identify in another way")
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
        if (enteredValue.equalsIgnoreCase("female")) {
            return Gender.female;
        } else if (enteredValue.equalsIgnoreCase("male")) {
            return Gender.male;
        } else if (enteredValue.equalsIgnoreCase("other")) {
            return Gender.other;
        }
        return null;
    }
}
