package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RelationshipsEnum {

    @JsonProperty("father")
    FATHER("father", "Father"),
    @JsonProperty("mother")
    MOTHER("mother", "Mother"),
    @JsonProperty("stepFather")
    STEPFATHER("stepFather", "Step-father"),
    @JsonProperty("stepMother")
    STEPMOTHER("stepMother", "Step-mother"),
    @JsonProperty("grandParent")
    GRANDPARENT("grandParent", "Grandparent"),
    @JsonProperty("guardian")
    GUARDIAN("guardian", "Guiardian"),
    @JsonProperty("specialGuardian")
    SPECIAL_GUARDIAN("specialGuardian", "Special Guardian"),
    @JsonProperty("other")
    OTHER("other", "Other");

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
