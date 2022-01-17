package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RelationshipsEnum {

    @JsonProperty("father")
    fether("father", "Father"),
    @JsonProperty("mother")
    mother("mother", "Mother"),
    @JsonProperty("stepFather")
    stepFather("stepFather", "Step-father"),
    @JsonProperty("stepMother")
    stepMother("stepMother", "Step-mother"),
    @JsonProperty("grandParent")
    grandParent("grandParent", "Grandparent"),
    @JsonProperty("guardian")
    guardian("guardian", "Guiardian"),
    @JsonProperty("specialGuardian")
    specialGuardian("specialGuardian", "Special Guardian"),
    @JsonProperty("other")
    other("other", "Other");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static RelationshipsEnum getValue(String key) {
        return RelationshipsEnum.valueOf(key);
    }

}
