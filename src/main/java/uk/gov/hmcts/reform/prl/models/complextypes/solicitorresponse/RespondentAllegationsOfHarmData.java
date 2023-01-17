package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class RespondentAllegationsOfHarmData {

    private final RespondentAllegationsOfHarm respondentAllegationsOfHarm;
    private final List<Element<Behaviours>> respondentDomesticAbuseBehaviour;
    private final List<Element<Behaviours>> respondentChildAbuseBehaviour;
    private final RespondentChildAbduction respondentChildAbduction;
    private final RespondentOtherConcerns respondentOtherConcerns;
}
