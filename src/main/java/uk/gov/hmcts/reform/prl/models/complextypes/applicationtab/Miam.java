package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder
@Data
public class Miam {
    @CCD(label = "Has the applicant attended MIAM?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo applicantAttendedMiam;
    @CCD(
            label = "Is the applicant claiming exemption from the requirement to attend a MIAM?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo claimingExemptionMiam;
    @CCD(
            label = "Has a family mediator informed the applicant that a mediator’s exemption applied, and they do not need to attend a MIAM?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo familyMediatorMiam;
    @CCD(label = "MIAM Registration number (URN)", searchable = false)
    private final String mediatorRegistrationNumber;
    @CCD(label = "Family mediation service name", searchable = false)
    private final String familyMediatorServiceName;
    @CCD(label = "soleTraderName", searchable = false)
    private final String soleTraderName;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Has the respondent attended a Mediation Information and Assessment Meeting (MIAM)?", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo attendedMiam;
  @CCD(label = "Would they be willing to attend a MIAM?", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo willingToAttendMiam;
  @CCD(label = "Explain why", searchable = false)
  private String reasonNotAttendingMiam;
  // ==== end synthesised definition-only fields ====
}
