package uk.gov.hmcts.reform.prl.enums.respondentsolicitor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum RespondentWelshNeedsListEnum {
    @JsonProperty("speakWelsh")
    speakWelsh("Will need to speak Welsh"),

    @JsonProperty("readAndWriteWelsh")
    readAndWriteWelsh("Will need to read and write in Welsh");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static RespondentWelshNeedsListEnum getValue(String key) {
        return RespondentWelshNeedsListEnum.valueOf(key);
    }
}
