package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SpokenOrWrittenWelshEnum {

    @JsonProperty("spoken")
    spoken("Will need to speak Welsh"),
    @JsonProperty("written")
    written("Will need to read and write in Welsh"),
    @JsonProperty("both")
    both("Both");

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
