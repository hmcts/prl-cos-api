package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RespondentBehaviourTable {
    private final String applicantWantToStopFromRespondentDoing;
    private final String applicantWantToStopFromRespondentDoingToChild;
    private final String otherReasonApplicantWantToStopFromRespondentDoing;
}
