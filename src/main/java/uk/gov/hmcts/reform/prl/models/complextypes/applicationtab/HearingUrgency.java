package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Builder
@Data
public class HearingUrgency {

    public final YesOrNo isCaseUrgent;
    public final String caseUrgencyTimeAndReason;
    public final String effortsMadeWithRespondents;
    public final YesOrNo doYouNeedAWithoutNoticeHearing;
    public final String reasonsForApplicationWithoutNotice;
    public final YesOrNo doYouRequireAHearingWithReducedNotice;
    public final String setOutReasonsBelow;
    public final YesOrNo areRespondentsAwareOfProceedings;

}
