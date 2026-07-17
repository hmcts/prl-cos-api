package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.FurtherEvidenceDocumentType;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class FurtherEvidence {
    @CCD(
            label = "Type of document",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "docTypeFurtherEvidence"
    )
    private final FurtherEvidenceDocumentType typeOfDocumentFurtherEvidence;
    @CCD(label = "Upload document", categoryID = "applicantApplication", searchable = false)
    private final Document documentFurtherEvidence;
    @CCD(label = " ", searchable = false)
    private final List<RestrictToCafcassHmcts> restrictCheckboxFurtherEvidence;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "  ", searchable = false, typeOverride = FieldType.Label)
  private String checkDocumentsConfidentialLabel;
  // ==== end synthesised definition-only fields ====
}
