package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ApplicantRelationshipDescriptionEnum {

    @JsonProperty("marriedOrCivil")
    marriedOrCivil("marriedOrCivil", "Married or in a civil partnership"),
    @JsonProperty("formerlyMarriedOrCivil")
    formerlyMarriedOrCivil("formerlyMarriedOrCivil", "Formerly married or in a civil partnership"),
    @JsonProperty("engagedOrProposed")
    engagedOrProposed("engagedOrProposed", "Engaged or proposed civil partnership"),
    @JsonProperty("formerlyEngagedOrProposed")
    formerlyEngagedOrProposed("formerlyEngagedOrProposed", "Formerly engaged or proposed civil partnership"),
    @JsonProperty("liveTogetherCouple")
    liveTogetherCouple("liveTogetherCouple", "Live together as a couple"),
    @JsonProperty("formerlyLiveTogetherCouple")
    formerlyLiveTogetherCouple("formerlyLiveTogetherCouple", "Formerly lived together as a couple"),
    @JsonProperty("currentNotLiveTogether")
    currentNotLiveTogether("currentNotLiveTogether", "Boyfriend, girlfriend or partner who does not live with them"),
    @JsonProperty("formerNotLiveTogether")
    formerNotLiveTogether("formerNotLiveTogether", "Formerly boyfriend, girlfriend or"
                                            + " partner who has not lived with them"),
    @JsonProperty("noneOfAbove")
    noneOfAbove("noneOfAbove", "None of the above");

    private final String id;
    private final String displayedValue;

    public String getId() {
        return id;
    }

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ApplicantRelationshipDescriptionEnum getValue(String key) {
        return ApplicantRelationshipDescriptionEnum.valueOf(key);
    }
}
