package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentSolicitorAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorInternationalElement;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class SolicitorResponse {

    private final Consent respondentConsentToApplication;
    private final KeepDetailsPrivate respondentKeepDetailsPrivate;
    private final AttendToCourt attendingTheCourt;
    private final SolicitorInternationalElement solicitorInternationalElement;
    private final RespondentSolicitorAllegationsOfHarm respondentSolicitorAllegationsOfHarm;

}
