package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum LiveWithEnum {

    @JsonProperty("applicant")
    APPLICANT("applicant", "Applicant"),
    @JsonProperty("respondent")
    RESPONDENT("respondent", "Respondent"),
    @JsonProperty("anotherPerson")
    ANOTHER_PERSON("anotherPerson", "Another person not listed");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static OrderTypeEnum getValue(String key) {
        return OrderTypeEnum.valueOf(key);
    }
}
