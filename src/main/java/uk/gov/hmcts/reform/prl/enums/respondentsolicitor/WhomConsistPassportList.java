package uk.gov.hmcts.reform.prl.enums.respondentsolicitor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum WhomConsistPassportList {
    @CCD(label = "Mother")
    @JsonProperty("mother")
    mother("mother", "Mother"),

    @CCD(label = "Father")
    @JsonProperty("father")
    father("father", "Father"),

    @CCD(label = "Other")
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
