package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum RelationshipsEnum {

    @CCD(label = "Father")
    @JsonProperty("father")
    father("father", "Father"),
    @CCD(label = "Mother")
    @JsonProperty("mother")
    mother("mother", "Mother"),
    @CCD(label = "Step-father")
    @JsonProperty("stepFather")
    stepFather("stepFather", "Step-father"),
    @CCD(label = "Step-mother")
    @JsonProperty("stepMother")
    stepMother("stepMother", "Step-mother"),
    @CCD(label = "Grandparent")
    @JsonProperty("grandParent")
    grandParent("grandParent", "Grandparent"),
    @CCD(label = "Guardian")
    @JsonProperty("guardian")
    guardian("guardian", "Guardian"),
    @CCD(label = "Special Guardian")
    @JsonProperty("specialGuardian")
    specialGuardian("specialGuardian", "Special Guardian"),
    @CCD(label = "Other")
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
