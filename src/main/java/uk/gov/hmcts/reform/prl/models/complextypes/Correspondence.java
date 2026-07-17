package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class Correspondence {
    @CCD(
            label = "Document name",
            hint = "Add a descriptive name. For example, \"letters from social workers\" or \"letter from doctor\"",
            searchable = false
    )
    @JsonProperty("documentName")
    private final String documentName;
    @CCD(label = "Notes", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("notes")
    private final String notes;
    @CCD(label = "Upload document", categoryID = "anyOtherDoc", searchable = false)
    private final Document documentCorrespondence;
    @CCD(label = " ", searchable = false)
    private final List<RestrictToCafcassHmcts> restrictCheckboxCorrespondence;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String checkDocumentsConfidentialLabel;
  // ==== end synthesised definition-only fields ====
}
