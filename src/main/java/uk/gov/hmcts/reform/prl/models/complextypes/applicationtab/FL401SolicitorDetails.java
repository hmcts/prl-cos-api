package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder
@Data
public class FL401SolicitorDetails {
    @CCD(label = "*Legal representative's first name", searchable = false)
    private final String representativeFirstName;
    @CCD(label = "*Legal representative's last name", searchable = false)
    private final String representativeLastName;
    @CCD(label = "*Email address", searchable = false, typeOverride = FieldType.Email)
    private final String solicitorEmail;
    @CCD(label = "Legal representative's contact number", searchable = false)
    private final String solicitorTelephone;
    @CCD(label = "Legal representative's reference(optional)", searchable = false)
    private final String solicitorReference;
    @CCD(label = "DX Number(optional)", searchable = false)
    private final String dxNumber;
    @CCD(label = "Organisation name", searchable = false)
    private final Organisation solicitorOrg;

    @CCD(label = "First names", searchable = false)
    private final String barristerFirstName;
    @CCD(label = "Last name", searchable = false)
    private final String barristerLastName;
    @CCD(label = "email address", searchable = false, typeOverride = FieldType.Email)
    private final String barristerEmail;
    @CCD(label = "Organisation", searchable = false)
    private final Organisation barristerOrg;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "### Applicant barrister",
          showCondition = "barristerFirstName!=\"\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String barristerLabel;
  // ==== end synthesised definition-only fields ====
}

