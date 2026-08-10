package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum NewPassportPossessionEnum {

    @CCD(label = "Mother")
    mother("Mother"),
    @CCD(label = "Father")
    father("Father"),
    @CCD(label = "Other")
    otherPerson("Other");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static NewPassportPossessionEnum getValue(String key) {
        return NewPassportPossessionEnum.valueOf(key);
    }
}
