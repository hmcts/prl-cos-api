package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum PassportPossessionEnum {

    @CCD(label = "Mother")
    mother("mother", "Mother"),
    @CCD(label = "Father")
    father("father", "Father"),
    @CCD(label = "Other")
    otherPerson("otherPerson", "Other");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static PassportPossessionEnum getValue(String key) {
        return PassportPossessionEnum.valueOf(key);
    }
}
