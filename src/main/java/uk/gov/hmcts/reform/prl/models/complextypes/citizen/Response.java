package uk.gov.hmcts.reform.prl.models.complextypes.citizen;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenFlags;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.abilitytoparticipate.AbilityToParticipate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.internationalelements.CitizenInternationalElements;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.miam.Miam;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings.CurrentOrPreviousProceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.safetyconcerns.SafetyConcerns;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.supportyouneed.ReasonableAdjustmentsSupport;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentProceedingDetails;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JasonIgnore(null)
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

    // TODO: Add safety Concerns here
    private final CitizenInternationalElements citizenInternationalElements;
    private final CitizenFlags citizenFlags;
    private final String safeToCallOption;
    private final SafetyConcerns safetyConcerns;
    private final ReasonableAdjustmentsSupport supportYouNeed;

    private final YesNoDontKnow currentOrPastProceedingsForChildren;
    private final List<Element<RespondentProceedingDetails>> respondentExistingProceedings;
    @JsonUnwrapped
    @Builder.Default
    private final AbilityToParticipate abilityToParticipate;
    private final AttendToCourt attendToCourt;

    @JsonUnwrapped
    @Builder.Default
    private final RespondentAllegationsOfHarmData respondentAllegationsOfHarmData;
    private final ResponseToAllgeationsofHarm responseToAllgeationsofHarm;

    private final YesOrNo c7ResponseSubmitted;
    private final YesOrNo c1AResponseSubmitted;

    private final YesOrNo activeRespondent;
}
