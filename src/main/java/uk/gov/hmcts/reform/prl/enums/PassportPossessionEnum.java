package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PassportPossessionEnum {

    mother("Mother"),
    father("Father"),
    otherPerson("Other");

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
