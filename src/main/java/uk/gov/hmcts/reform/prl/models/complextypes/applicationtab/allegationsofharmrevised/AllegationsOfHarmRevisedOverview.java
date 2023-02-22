package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Builder
@Data
public class AllegationsOfHarmRevisedOverview {
    private final YesOrNo newAllegationsOfHarmYesNo;
    private final YesOrNo newAllegationsOfHarmDomesticAbuseYesNo;
    private final YesOrNo newAllegationsOfHarmChildAbductionYesNo;
    private final YesOrNo newAllegationsOfHarmChildAbuseYesNo;
    private final YesOrNo newAllegationsOfHarmSubstanceAbuseYesNo;
    private final String newAllegationsOfHarmSubstanceAbuseDetails;
    private final YesOrNo newAllegationsOfHarmOtherConcerns;
    private final String newAllegationsOfHarmOtherConcernsDetails;

}
