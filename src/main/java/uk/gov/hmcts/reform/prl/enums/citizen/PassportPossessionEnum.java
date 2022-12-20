package uk.gov.hmcts.reform.prl.enums.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PassportPossessionEnum {
    Mother("Mother"),
    Father("Father"),
    Other("Other");

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
