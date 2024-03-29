package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ApplicantRelationshipEnum {

    @JsonProperty("marriedOrCivil")
    marriedOrCivil("marriedOrCivil", "Married or in a civil partnership"),
    @JsonProperty("formerlyMarriedOrCivil")
    formerlyMarriedOrCivil("formerlyMarriedOrCivil", "Formerly married or in a civil partnership"),
    @JsonProperty("engagedOrProposed")
    engagedOrProposed("engagedOrProposed", "Engaged or proposed civil partnership"),
    @JsonProperty("formerlyEngagedOrProposed")
    formerlyEngagedOrProposed("formerlyEngagedOrProposed", "Formerly engaged or proposed civil partnership"),
    @JsonProperty("liveTogether")
    liveTogether("liveTogether", "Live together as a couple"),
    @JsonProperty("foremerlyLivedTogether")
    foremerlyLivedTogether("foremerlyLivedTogether", "Formerly lived together as a couple"),
    @JsonProperty("bfGfOrPartnerNotLivedTogether")
    bfGfOrPartnerNotLivedTogether("bfGfOrPartnerNotLivedTogether", "Boyfriend, girlfriend or partner who does not live with them"),
    @JsonProperty("formerBfGfOrPartnerNotLivedTogether")
    formerBfGfOrPartnerNotLivedTogether("formerBfGfOrPartnerNotLivedTogether", "Formerly boyfriend, girlfriend or"
        + " partner who has not lived with them"),
    @JsonProperty("noneOfTheAbove")
    noneOfTheAbove("noneOfTheAbove", "None of the above");

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
    public static ApplicantRelationshipEnum getValue(String key) {
        return ApplicantRelationshipEnum.valueOf(key);
    }

    public static ApplicantRelationshipEnum getDisplayedValueFromEnumString(String enteredValue) {
        if (enteredValue.equalsIgnoreCase("noneOfAbove")) {
            return ApplicantRelationshipEnum.noneOfTheAbove;
        } else if (enteredValue.equalsIgnoreCase("formerNotLiveTogether")) {
            return ApplicantRelationshipEnum.formerBfGfOrPartnerNotLivedTogether;
        } else if (enteredValue.equalsIgnoreCase("currentNotLiveTogether")) {
            return ApplicantRelationshipEnum.bfGfOrPartnerNotLivedTogether;
        } else if (enteredValue.equalsIgnoreCase("formerlyLiveTogetherCouple")) {
            return ApplicantRelationshipEnum.foremerlyLivedTogether;
        } else if (enteredValue.equalsIgnoreCase("liveTogetherCouple")) {
            return ApplicantRelationshipEnum.liveTogether;
        } else if (enteredValue.equalsIgnoreCase("formerlyEngagedOrProposed")) {
            return ApplicantRelationshipEnum.formerlyEngagedOrProposed;
        } else if (enteredValue.equalsIgnoreCase("engagedOrProposed")) {
            return ApplicantRelationshipEnum.engagedOrProposed;
        } else if (enteredValue.equalsIgnoreCase("formerlyMarriedOrCivil")) {
            return ApplicantRelationshipEnum.formerlyMarriedOrCivil;
        } else if (enteredValue.equalsIgnoreCase("marriedOrCivil")) {
            return ApplicantRelationshipEnum.marriedOrCivil;
        }
        return null;
    }
}
