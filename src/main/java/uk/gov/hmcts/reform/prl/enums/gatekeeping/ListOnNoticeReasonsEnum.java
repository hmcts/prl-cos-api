package uk.gov.hmcts.reform.prl.enums.gatekeeping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ListOnNoticeReasonsEnum {

    @JsonProperty("localAuthorityInvolvedWithTheChildrenAndFamily")
    localAuthorityInvolvedWithTheChildrenAndFamily("The Local Authority are currently involved with the child[ren] and family"),
    @JsonProperty("noEvidenceOfImmediateRiskOfHarmToTheChildren")
    noEvidenceOfImmediateRiskOfHarmToTheChildren("There is no evidence of immediate risk of harm to the child[ren]"),
    @JsonProperty("bothPartiesInformationAndSafeguardingNecessaryToCourt")
    bothPartiesInformationAndSafeguardingNecessaryToCourt(
        "Information from both parties and safeguarding is necessary to enable the court to determine the long-term arrangements."),
    @JsonProperty("childrenResideWithApplicantAndBothProtectedByNonMolestationOrder")
    childrenResideWithApplicantAndBothProtectedByNonMolestationOrder(
        "The child[ren] reside with applicant and both are protected by a Non-Molestation Order"),
    @JsonProperty("noEvidenceOnRespondentSeeksToRemovetheChildrenFromApplicantsCare")
    noEvidenceOnRespondentSeeksToRemovetheChildrenFromApplicantsCare(
        "There is no evidence to suggest that the respondent seeks to remove the child[ren] from the applicant's care and therefore "
            + "there is no genuine emergency"),
    @JsonProperty("noEvidenceOnRespondentSeekToFrustrateTheProcessIfTheyWereGivenNotice")
    noEvidenceOnRespondentSeekToFrustrateTheProcessIfTheyWereGivenNotice(
        "There is no evidence to suggest that the respondent would seek to frustrate the process if they were given notice"),
    @JsonProperty("itsNotWithoutNoticeButItsUrgent")
    itsNotWithoutNoticeButItsUrgent("It is not without notice but it is urgent");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static String getDisplayedValue(String key) {
        return valueOf(key).displayedValue;
    }
}
