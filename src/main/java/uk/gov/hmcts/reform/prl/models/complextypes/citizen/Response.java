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
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResponseToAllegationsOfHarm;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentChildAbduction;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentOtherConcerns;
import uk.gov.hmcts.reform.prl.models.dto.ccd.SolicitorInternationalElement;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Response {
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo legalRepresentation;
    @CCD(label = " ", searchable = false)
    private final Consent consent;
    @CCD(label = " ", searchable = false)
    private final KeepDetailsPrivate keepDetailsPrivate;
    @CCD(label = " ", searchable = false)
    private final CitizenDetails citizenDetails;
    // TODO: Add support you need during your case here
    @CCD(label = " ", searchable = false)
    private final Miam miam;
    //Applicable only for C100 citizen respondent
    @CCD(label = " ", searchable = false)
    private final CurrentOrPreviousProceedings currentOrPreviousProceedings;

    // TODO: Add safety Concerns here
    @CCD(label = " ", searchable = false)
    private final CitizenInternationalElements citizenInternationalElements;
    @CCD(label = " ", searchable = false)
    private CitizenFlags citizenFlags;
    @CCD(label = " ", searchable = false)
    private final String safeToCallOption;
    @CCD(label = " ", searchable = false)
    private final SafetyConcerns safetyConcerns;
    @CCD(label = " ", searchable = false)
    private final ReasonableAdjustmentsSupport supportYouNeed;

    @CCD(label = " ", searchable = false)
    private final YesNoDontKnow currentOrPastProceedingsForChildren;
    @CCD(label = " ", searchable = false)
    private final List<Element<RespondentProceedingDetails>> respondentExistingProceedings;
    @JsonUnwrapped
    @Builder.Default
    private final AbilityToParticipate abilityToParticipate;
    @CCD(label = " ", searchable = false)
    private final AttendToCourt attendToCourt;

    @JsonUnwrapped
    @Builder.Default
    private final RespondentAllegationsOfHarmData respondentAllegationsOfHarmData;
    @CCD(label = " ", searchable = false)
    private String respondingCitizenAoH;

    @JsonUnwrapped
    @Builder.Default
    private final ResponseToAllegationsOfHarm responseToAllegationsOfHarm;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo c7ResponseSubmitted;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo c1AResponseSubmitted;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo activeRespondent;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false)
  private KeepDetailsPrivate respKeepDetailsPrivate;
  @CCD(label = " ", searchable = false)
  private KeepDetailsPrivate respKeepDetailsPrivateConfidentiality;
  @CCD(label = " ", searchable = false)
  private uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Miam respSolHaveYouAttendedMiam;
  @CCD(label = " ", searchable = false)
  private uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Miam respSolWillingnessToAttendMiam;
  @CCD(label = " ", searchable = false)
  private RespondentAllegationsOfHarm respAllegationsOfHarmInfo;
  @CCD(label = " ", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Behaviours>> respDomesticAbuseInfo;
  @CCD(label = " ", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Behaviours>> respChildAbuseInfo;
  @CCD(label = " ", searchable = false)
  private RespondentChildAbduction respChildAbductionInfo;
  @CCD(label = " ", searchable = false)
  private RespondentOtherConcerns respOtherConcernsInfo;
  @CCD(label = " ", searchable = false)
  private SolicitorInternationalElement internationalElementChildInfo;
  @CCD(label = " ", searchable = false)
  private SolicitorInternationalElement internationalElementParentInfo;
  @CCD(label = " ", searchable = false)
  private SolicitorInternationalElement internationalElementJurisdictionInfo;
  @CCD(label = " ", searchable = false)
  private SolicitorInternationalElement internationalElementRequestInfo;
  // ==== end synthesised definition-only fields ====
}
