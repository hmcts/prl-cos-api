package uk.gov.hmcts.reform.prl.enums.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LanguageRequirementsEnum {

    speakwelsh("I need to speak in Welsh"),
    readandwritewelsh("I need to read and write in Welsh"),
    languageinterpreter("I need an interpreter in a certain language"),
    nointerpreter("No, I do not have any language requirements at this time");

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
