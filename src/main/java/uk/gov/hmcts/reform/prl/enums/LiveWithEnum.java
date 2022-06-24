package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum LiveWithEnum {

    @JsonProperty("applicant")
    applicant("applicant", "Applicant"),
    @JsonProperty("respondent")
    respondent("respondent", "Respondent"),
    @JsonProperty("anotherPerson")
    anotherPerson("anotherPerson", "Another person not listed");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static LiveWithEnum getValue(String key) {
        return LiveWithEnum.valueOf(key);
    }
}
