package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum BehaviourTowardsChildrenEnum {

    @JsonProperty("beingViolentOrThreatening")
    beingViolentOrThreatening("Being violent or threatening towards their child or children"),
    @JsonProperty("harrasingOrIntimidating")
    harrasingOrIntimidating("Harassing or intimidating their child or children"),
    @JsonProperty("publishingAboutChildren")
    publishingAboutChildren("Posting or publishing anything about their child or children in print or digitally"),
    @JsonProperty("contactingDirectly")
    contactingDirectly("Contacting their child or children directly without the applicant’s consent"),
    @JsonProperty("goingNearSchoolNursery")
    goingNearSchoolNursery("Going to or near the child or child's school or Nursery");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static BehaviourTowardsChildrenEnum getValue(String key) {
        return BehaviourTowardsChildrenEnum.valueOf(key);
    }

}
