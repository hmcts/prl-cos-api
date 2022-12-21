package uk.gov.hmcts.reform.prl.enums.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourtComfortEnum {

    appropriatelighting("Appropriate lighting"),
    breaks("Regular breaks"),
    space("Space to be able to get up and move around"),
    other("Other"),
    nosupport("No, I do not need any extra support at this time");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static CourtComfortEnum getValue(String key) {
        return CourtComfortEnum.valueOf(key);
    }
}
