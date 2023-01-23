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

    private final YesOrNo respondentAohYesOrNo;
    private final RespondentAllegationsOfHarm respondentAllegationsOfHarmInfo;
    private final List<Element<Behaviours>> respondentDomesticAbuseBehaviourInfo;
    private final List<Element<Behaviours>> respondentChildAbuseBehaviourInfo;
    private final RespondentChildAbduction respondentChildAbductionInfo;
    private final RespondentOtherConcerns respondentOtherConcernsInfo;
}
