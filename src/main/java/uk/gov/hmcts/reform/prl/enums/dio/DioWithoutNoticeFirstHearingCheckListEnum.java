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
public enum DioWithoutNoticeFirstHearingCheckListEnum {

    @CCD(label = "There is evidence of immediate risk of harm to the child[ren]")
    @JsonProperty("evidenceOfImmediateRisk")
    evidenceOfImmediateRisk("evidenceOfImmediateRisk", "There is evidence of immediate risk of harm to the child[ren]"),
    @CCD(
            label = "There is evidence to suggest that the respondent seeks to remove the child[ren] from the applicant's care"
    )
    @JsonProperty("evidenceOfRemoveFromApplicantsCare")
    evidenceOfRemoveFromApplicantsCare("evidenceOfRemoveFromApplicantsCare", "There is evidence to "
        + "suggest that the respondent seeks to remove the child[ren] from the applicant's care"),
    @CCD(
            label = "There is evidence to suggest that the respondent would seek to frustrate the process if they were given notice of the application"
    )
    @JsonProperty("evidenceThatNoticeOfApplication")
    evidenceThatNoticeOfApplication("evidenceThatNoticeOfApplication", "There is evidence to suggest "
        + "that the respondent would seek to frustrate the process if they were given notice of the application"),
    @CCD(
            label = "There is evidence to suggest the respondent may attempt to leave the jurisdiction with the child[ren]"
    )
    @JsonProperty("evidenceForRespondentToLeave")
    evidenceForRespondentToLeave("evidenceForRespondentToLeave", "There is evidence to suggest the "
        + "respondent may attempt to leave the jurisdiction with the child[ren]"),
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
    public static DioWithoutNoticeFirstHearingCheckListEnum getValue(String key) {
        return DioWithoutNoticeFirstHearingCheckListEnum.valueOf(key);
    }

}
