package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
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

}
