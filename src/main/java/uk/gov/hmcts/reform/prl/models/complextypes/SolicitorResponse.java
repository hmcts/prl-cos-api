package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentSolicitorAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorInternationalElement;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class SolicitorResponse {

    private final AttendToCourt attendingTheCourt;
    private final SolicitorInternationalElement solicitorInternationalElement;
    private final RespondentSolicitorAllegationsOfHarm respondentSolicitorAllegationsOfHarm;

}
