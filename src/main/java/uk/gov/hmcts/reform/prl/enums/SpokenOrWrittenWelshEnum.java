package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SpokenOrWrittenWelshEnum {

    @JsonProperty("spoken")
    Spoken("Will need to speak Welsh"),
    @JsonProperty("written")
    Written("Will need to read and write in Welsh"),
    @JsonProperty("both")
    Both("Both");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SpokenOrWrittenWelshEnum getValue(String key) {
        return SpokenOrWrittenWelshEnum.valueOf(key);
    }

}
