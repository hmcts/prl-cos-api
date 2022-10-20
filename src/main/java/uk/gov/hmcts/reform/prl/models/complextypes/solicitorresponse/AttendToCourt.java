package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.solicitorresponse.RespondentWelshNeedsListEnum;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class AttendToCourt {
    private final YesOrNo respondentWelshNeeds;
    private final RespondentWelshNeedsListEnum respondentWelshNeedsList;
    private final YesOrNo isRespondentNeededInterpreter;
    private final RespondentInterpreterNeeds respondentInterpreterNeeds;
    private final YesOrNo haveAnyDisability;
    private final String disabilityNeeds;
    private final YesOrNo respondentSpecialArrangements;
    private final String respondentSpecialArrangementDetails;
    private final YesOrNo respondentIntermediaryNeeds;
    private final String respondentIntermediaryNeedDetails;

}
