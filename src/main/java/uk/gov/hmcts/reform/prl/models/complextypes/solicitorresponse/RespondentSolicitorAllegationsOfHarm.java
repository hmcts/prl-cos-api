package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class RespondentSolicitorAllegationsOfHarm {

    private final RespondentAllegationsOfHarm respondentAllegationsOfHarm;
    private final Behaviours respondentDomesticAbuseBehaviour;
    private final Behaviours respondentChildAbuseBehaviour;
    private final RespondentChildAbduction respondentChildAbduction;
    private final RespondentOtherConcerns respondentOtherConcerns;
}
