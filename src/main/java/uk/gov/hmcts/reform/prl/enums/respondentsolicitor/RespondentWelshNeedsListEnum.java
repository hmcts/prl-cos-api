package uk.gov.hmcts.reform.prl.enums.respondentsolicitor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum RespondentWelshNeedsListEnum {
    @CCD(label = "Will want to speak Welsh")
    @JsonProperty("speakWelsh")
    speakWelsh("Will want to speak Welsh"),

    @CCD(label = "Will want to read and write in Welsh")
    @JsonProperty("readAndWriteWelsh")
    readAndWriteWelsh("Will want to read and write in Welsh");

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
