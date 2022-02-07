package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Builder
@Data
public class AllegationsOfHarmOverview {

    private final YesOrNo allegationsOfHarmYesNo;
    private final YesOrNo allegationsOfHarmDomesticAbuseYesNo;
    private final YesOrNo allegationsOfHarmChildAbductionYesNo;
    private final YesOrNo allegationsOfHarmChildAbuseYesNo;
    private final YesOrNo allegationsOfHarmSubstanceAbuseYesNo;
    private final YesOrNo allegationsOfHarmOtherConcernsYesNo;

}
