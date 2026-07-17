package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class WelshNeed {
    @CCD(
            label = "*Provide the names of the people involved in the case who want to speak Welsh or read and write in Welsh.",
            searchable = false
    )
    private String whoNeedsWelsh;
    @CCD(label = " ", searchable = false)
    private  List<SpokenOrWrittenWelshEnum> spokenOrWritten;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "SpokenOrWrittenWelshFL401Enum"
    )
    private  List<SpokenOrWrittenWelshEnum> fl401SpokenOrWritten;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "## Add new welsh need", searchable = false, typeOverride = FieldType.Label)
  private String addNewWelshNeedLabel;
  // ==== end synthesised definition-only fields ====
}
