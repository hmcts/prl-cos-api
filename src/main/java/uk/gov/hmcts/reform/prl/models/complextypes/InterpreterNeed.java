package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class InterpreterNeed {

    @CCD(label = "*Who will require the interpreter?", searchable = false)
    private List<PartyEnum> party;
    @CCD(label = "  ", searchable = false)
    private String name;
    @CCD(label = "*Enter details of the language or dialect required.", searchable = false)
    private String language;
    @CCD(label = "Any other assistance required (e.g. sign language)", searchable = false)
    private String otherAssistance;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "*Describe their relationship to the case.\n\nFor example - grandmother of the applicant.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String nameLabel;
  @CCD(label = "## Add new interpreter need", searchable = false, typeOverride = FieldType.Label)
  private String addNewInterpreterNeedLabel;
  // ==== end synthesised definition-only fields ====
}
