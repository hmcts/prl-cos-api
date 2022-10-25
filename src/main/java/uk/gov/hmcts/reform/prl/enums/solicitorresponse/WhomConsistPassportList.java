package uk.gov.hmcts.reform.prl.enums.solicitorresponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum WhomConsistPassportList {
    @JsonProperty("mother")
    mother("mother", "Mother"),

    @JsonProperty("father")
    father("father", "Father"),

    @JsonProperty("otherPeople")
    otherPeople("otherPeople", "Other");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static WhomConsistPassportList getValue(String key) {
        return WhomConsistPassportList.valueOf(key);
    }
}
