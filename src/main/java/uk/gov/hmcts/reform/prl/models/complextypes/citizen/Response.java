package uk.gov.hmcts.reform.prl.models.complextypes.citizen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Miam;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.internationalElements.CitizenInternationalElements;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings.CurrentOrPreviousProceedings;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Response {
    private final YesOrNo legalRepresentation;
    private final Consent consent;
    private final KeepDetailsPrivate confidentiality;
    private final PartyDetails respondentDetails;
    // TODO: Add support you need during your case here
    private final Miam miam;
    private final CurrentOrPreviousProceedings currentOrPreviousProceedings;
    // TODO: Add safety Concerns here
    private final CitizenInternationalElements citizenInternationalElements;
}
