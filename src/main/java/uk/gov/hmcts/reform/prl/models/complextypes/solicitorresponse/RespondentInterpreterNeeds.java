package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class RespondentInterpreterNeeds {

    @CCD(label = "*Who will require the interpreter?", searchable = false)
    private final List<PartyEnum> party;
    @CCD(label = "  ", searchable = false)
    private final String relationName;
    @CCD(label = "*Enter details of the language or dialect required.", searchable = false)
    private final String requiredLanguage;
    @CCD(label = "Any other assistance required (e.g. sign language)", searchable = false)
    private final String respondentOtherAssistance;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "*Describe their relationship to the case.  \nFor example - grandmother of the respondent.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String relationNameLabel;
  @CCD(label = "## Add new interpreter need", searchable = false, typeOverride = FieldType.Label)
  private String addInterpreterNeedLabel;
  // ==== end synthesised definition-only fields ====
}
