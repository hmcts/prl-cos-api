package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class RespondentAllegationsOfHarmData {

    private final YesOrNo respAohYesOrNo;
    private final RespondentAllegationsOfHarm respAllegationsOfHarmInfo;
    private final List<Element<Behaviours>> respDomesticAbuseInfo;
    private final List<Element<Behaviours>> respChildAbuseInfo;
    private final RespondentChildAbduction respChildAbductionInfo;
    private final RespondentOtherConcerns respOtherConcernsInfo;
}
