package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;


@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum BehaviourTowardsApplicantEnum {

    @JsonProperty("beingViolentOrThreatening")
    beingViolentOrThreatening("Being violent or threatening towards them"),
    @JsonProperty("harrasingOrIntimidating")
    harrasingOrIntimidating(" Harassing or intimidating them"),
    @JsonProperty("publishingAboutApplicant")
    publishingAboutApplicant("Posting or publishing about them either in print or digitally"),
    @JsonProperty("contactingApplicant")
    contactingApplicant("Contacting them directly"),
    @JsonProperty("damagingPossessions")
    damagingPossessions("Causing damage to their possessions"),
    @JsonProperty("damagingHome")
    damagingHome("Causing damage to their home"),
    @JsonProperty("enteringHome")
    enteringHome("Coming into my home"),
    @JsonProperty("comingNearHome")
    comingNearHome("Coming near my home"),
    @JsonProperty("comingNearWork")
    comingNearWork("Coming near my place of work");


    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static BehaviourTowardsApplicantEnum getValue(String key) {
        return BehaviourTowardsApplicantEnum.valueOf(key);
    }

}
