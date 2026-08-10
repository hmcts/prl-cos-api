package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ApplicantRelationshipOptionsEnum {

    @CCD(label = "Father")
    @JsonProperty("father")
    father("father", "Father"),
    @CCD(label = "Mother")
    @JsonProperty("mother")
    mother("mother", "Mother"),
    @CCD(label = "Son")
    @JsonProperty("son")
    son("son", "Son"),
    @CCD(label = "Daughter")
    @JsonProperty("daughter")
    daughter("daughter", "Daughter"),
    @CCD(label = "Brother")
    @JsonProperty("brother")
    brother("brother", "Brother"),
    @CCD(label = "Sister")
    @JsonProperty("sister")
    sister("sister", "Sister"),
    @CCD(label = "Grandfather")
    @JsonProperty("grandfather")
    grandfather("grandfather", "Grandfather"),
    @CCD(label = "Grandmother")
    @JsonProperty("grandmother")
    grandmother("grandmother", "Grandmother"),
    @CCD(label = "Uncle")
    @JsonProperty("uncle")
    uncle("uncle", "Uncle"),
    @CCD(label = "Aunt")
    @JsonProperty("aunt")
    aunt("aunt", "Aunt"),
    @CCD(label = "Nephew")
    @JsonProperty("nephew")
    nephew("nephew", "Nephew"),
    @CCD(label = "Niece")
    @JsonProperty("niece")
    niece("niece", "Niece"),
    @CCD(label = "Cousin")
    @JsonProperty("cousin")
    cousin("cousin", "Cousin"),
    @CCD(label = "Other")
    @JsonProperty("other")
    other("other", "Other");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ApplicantRelationshipOptionsEnum getValue(String key) {
        return ApplicantRelationshipOptionsEnum.valueOf(key);
    }
}
