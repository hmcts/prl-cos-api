package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum RelationshipsEnum {

    @JsonProperty("father")
    father("father", "Father"),
    @JsonProperty("mother")
    mother("mother", "Mother"),
    @JsonProperty("stepFather")
    stepFather("stepFather", "Step-father"),
    @JsonProperty("stepMother")
    stepMother("stepMother", "Step-mother"),
    @JsonProperty("grandParent")
    grandParent("grandParent", "Grandparent"),
    @JsonProperty("guardian")
    guardian("guardian", "Guardian"),
    @JsonProperty("specialGuardian")
    specialGuardian("specialGuardian", "Special Guardian"),
    @JsonProperty("other")
    other("other", "Other");

    private final String id;
    private final String displayedValue;

    public static RelationshipsEnum getEnumForDisplayedValue(String displayedValue) {
        return Arrays.stream(RelationshipsEnum.values())
            .filter(relation -> relation.getDisplayedValue().equals(displayedValue))
            .findFirst()
            .orElse(other);
    }
}
