package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class RespondentBehaviour {
    private final List<ApplicantStopFromRespondentDoingEnum> applicantWantToStopFromRespondentDoing;
    private final List<ApplicantStopFromRespondentDoingToChildEnum> applicantWantToStopFromRespondentDoingToChild;
    private final String otherReasonApplicantWantToStopFromRespondentDoing;
}



