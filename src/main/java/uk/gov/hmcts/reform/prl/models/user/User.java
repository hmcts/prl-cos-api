package uk.gov.hmcts.reform.prl.models.user;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder
public class User {
    @CCD(ignore = true)
    private final String authorisation;
    @CCD(ignore = true)
    private final UserInfo userInfo;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false)
  private String idamId;
  @CCD(label = "PCQ ID", searchable = false)
  private String pcqId;
  @CCD(label = " ", searchable = false)
  private String email;
  @CCD(label = " ", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo solicitorRepresented;
  // ==== end synthesised definition-only fields ====
}
