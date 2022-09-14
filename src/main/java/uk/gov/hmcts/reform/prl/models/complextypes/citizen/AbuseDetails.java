package uk.gov.hmcts.reform.prl.models.complextypes.citizen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class AbuseDetails {
    private final String explainWhoWasInvolved;
    private final String whenDidBehaviourStart;
    private final YesOrNo isBehaviourStillGoingOn;
    private final YesOrNo haveYouEverAskedForHelp;
}
