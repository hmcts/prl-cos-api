package uk.gov.hmcts.reform.prl.enums.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SafetyArrangemensEnum {

    waitingroom("Separate waiting room"),
    separateexitentry("Separate exits and entrances"),
    screens("Screens so you and the other people in the case cannot see each other"),
    toilet("Separate toilets"),
    advancedview("Advanced viewing of the court"),
    videolinks("Video links"),
    other("Other"),
    nosupport("No, I do not need any extra support at this time");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SafetyArrangemensEnum getValue(String key) {
        return SafetyArrangemensEnum.valueOf(key);
    }
}
