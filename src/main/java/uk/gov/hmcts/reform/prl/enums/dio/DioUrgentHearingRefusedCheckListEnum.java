package uk.gov.hmcts.reform.prl.enums.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DioUrgentHearingRefusedCheckListEnum {

    @CCD(label = "The Local Authority are currently involved with the child[ren] and family")
    @JsonProperty("localAuthorivityInvolvedWithChildren")
    localAuthorivityInvolvedWithChildren("localAuthorivityInvolvedWithChildren", "The Local Authority "
        + "are currently involved with the child[ren] and family"),
    @CCD(label = "There is no evidence of immediate risk of harm to the child[ren]")
    @JsonProperty("noEvidenceOfImmediateRisk")
    noEvidenceOfImmediateRisk("noEvidenceOfImmediateRisk", "There is no evidence of immediate "
        + "risk of harm to the child[ren]"),
    @CCD(
            label = "Information from both parties and safeguarding is necessary to enable the court to determine the long-term arrangements."
    )
    @JsonProperty("safeguardingIsNecessary")
    safeguardingIsNecessary("safeguardingIsNecessary", "Information from both parties and safeguarding"
        + " is necessary to enable the court to determine the long-term arrangements."),
    @CCD(label = "The child[ren] reside with applicant and both are protected by a Non-Molestation Order ")
    @JsonProperty("protectedByNonMolestationOrder")
    protectedByNonMolestationOrder("protectedByNonMolestationOrder", "The child[ren] reside with "
        + "applicant and both are protected by a Non-Molestation Order "),
    @CCD(
            label = "There is no evidence to suggest that the respondent seeks to remove the child[ren] from the applicant's care and therefore there is no genuine emergency"
    )
    @JsonProperty("noGenuineEmergency")
    noGenuineEmergency("noGenuineEmergency", "There is no evidence to suggest that the respondent "
        + "seeks to remove the child[ren] from the applicant's care and therefore there is no genuine emergency"),
    @CCD(
            label = "There is no evidence to suggest that the respondent may attempt to leave the jurisdiction with the child[ren] if the application is not heard urgently"
    )
    @JsonProperty("noEvidenceForRespondentToLeave")
    noEvidenceForRespondentToLeave("noEvidenceForRespondentToLeave", "There is no evidence to suggest"
        + " that the respondent may attempt to leave the jurisdiction with the child[ren] if the application is not heard urgently"),
    @CCD(label = "Another reason that has not been listed")
    @JsonProperty("anotherReason")
    anotherReason("anotherReason", "Another reason that has not been listed");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DioUrgentHearingRefusedCheckListEnum getValue(String key) {
        return DioUrgentHearingRefusedCheckListEnum.valueOf(key);
    }

}
