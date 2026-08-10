package uk.gov.hmcts.reform.prl.enums.citizen;

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
public enum LanguageRequirementsEnum {

    @CCD(label = "I need to speak in Welsh")
    @JsonProperty("speakwelsh")
    speakwelsh("speakwelsh","I need to speak in Welsh"),
    @CCD(label = "I need to read and write in Welsh")
    @JsonProperty("readandwritewelsh")
    readandwritewelsh("readandwritewelsh","I need to read and write in Welsh"),
    @CCD(label = "I need an interpreter in a certain language")
    @JsonProperty("languageinterpreter")
    languageinterpreter("languageinterpreter","I need an interpreter in a certain language"),
    @CCD(label = "No, I do not have any language requirements at this time")
    @JsonProperty("nointerpreter")
    nointerpreter("nointerpreter","No, I do not have any language requirements at this time");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static LanguageRequirementsEnum getValue(String key) {
        return LanguageRequirementsEnum.valueOf(key);
    }
}
