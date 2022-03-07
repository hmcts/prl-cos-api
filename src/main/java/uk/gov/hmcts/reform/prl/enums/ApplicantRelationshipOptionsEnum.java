package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ApplicantRelationshipOptionsEnum {

    @JsonProperty("father")
    father("father", "Father"),
    @JsonProperty("mother")
    mother("mother", "Mother"),
    @JsonProperty("son")
    son("son", "Son"),
    @JsonProperty("daughter")
    daughter("daughter", "Daughter"),
    @JsonProperty("brother")
    brother("brother", "Brother"),
    @JsonProperty("sister")
    sister("sister", "Sister"),
    @JsonProperty("grandfather")
    grandfather("grandfather", "Grandfather"),
    @JsonProperty("grandmother")
    grandmother("grandmother", "Grandmother"),
    @JsonProperty("uncle")
    uncle("uncle", "Uncle"),
    @JsonProperty("aunt")
    aunt("aunt", "Aunt"),
    @JsonProperty("nephew")
    nephew("nephew", "Nephew"),
    @JsonProperty("neice")
    neice("neice", "Neice"),
    @JsonProperty("cousin")
    cousin("cousin", "Cousin"),
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
