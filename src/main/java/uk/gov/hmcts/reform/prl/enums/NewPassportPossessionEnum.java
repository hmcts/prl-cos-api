package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum NewPassportPossessionEnum {

    mother("Mother"),
    father("Father"),
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
