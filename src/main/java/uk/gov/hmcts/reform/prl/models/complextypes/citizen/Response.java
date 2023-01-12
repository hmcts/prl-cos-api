package uk.gov.hmcts.reform.prl.models.complextypes.citizen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenFlags;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.internationalelements.CitizenInternationalElements;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.miam.Miam;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings.CurrentOrPreviousProceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Response {
    private final YesOrNo legalRepresentation;
    private final Consent consent;
    private final KeepDetailsPrivate keepDetailsPrivate;
    private final CitizenDetails citizenDetails;
    // TODO: Add support you need during your case here
    private final Miam miam;
    //Applicable only for C100 citizen respondent
    private final CurrentOrPreviousProceedings currentOrPreviousProceedings;
    //Applicable only for C100 solicitor respondent
    private final YesNoDontKnow currentOrPastProceedingsForChildren;
    private final List<Element<ProceedingDetails>> respondentExistingProceedings;
    // TODO: Add safety Concerns here
    private final CitizenInternationalElements citizenInternationalElements;
    // TODO: Need to recheck this one
    // private final AbilityToParticipate abilityToParticipate;
    private final AttendToCourt respondentAttendingTheCourt;

    private final CitizenFlags citizenFlags;
    private final String safeToCallOption;

    private final YesOrNo activeRespondent;
    private final YesOrNo c7ResponseSubmitted;
    private final YesOrNo c1AResponseSubmitted;
}
