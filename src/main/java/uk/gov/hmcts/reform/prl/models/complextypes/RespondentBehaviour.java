package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
@AllArgsConstructor
public class RespondentBehaviour {
    @CCD(
            label = "What does the applicant want to stop the respondent from doing?",
            searchable = false,
            typeOverride = FieldType.Text
    )
    private final List<ApplicantStopFromRespondentDoingEnum> applicantWantToStopFromRespondentDoing;
    @CCD(
            label = "What does the applicant want the respondent to stop doing to their child or children (if applicable)?",
            searchable = false,
            typeOverride = FieldType.Text
    )
    private final List<ApplicantStopFromRespondentDoingToChildEnum> applicantWantToStopFromRespondentDoingToChild;
    @CCD(
            label = "Is there anything else that the applicant wants the respondent to stop doing, that is not mentioned in the questions above?",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String otherReasonApplicantWantToStopFromRespondentDoing;
}



