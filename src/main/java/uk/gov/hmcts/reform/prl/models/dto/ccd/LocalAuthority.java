package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder
@Data
public class LocalAuthority {

    @CCD(label = "Local Authority involvement", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("isLocalAuthorityInvolvedInCase")
    private YesOrNo isLocalAuthorityInvolvedInCase;

    @CCD(label = "Local Authority Organisation Name", searchable = false)
    @JsonProperty("localAuthoritySolicitorOrganisationName")
    private String localAuthoritySolicitorOrganisationName;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Local Authority", searchable = false)
  private String localAuthority;
  // ==== end synthesised definition-only fields ====
}
