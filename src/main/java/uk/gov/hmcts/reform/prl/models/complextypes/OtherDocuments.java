package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocTypeOtherDocumentsEnum2;

@Data
@Builder
public class OtherDocuments {
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
    private final Document documentOther;
    @CCD(
            label = "Choose a further evidence document type",
            searchable = false,
            typeParameterOverride = "docTypeOtherDocumentsEnum",
            typeParameterClass = DocTypeOtherDocumentsEnum2.class
    )
    private final DocTypeOtherDocumentsEnum documentTypeOther;
    @CCD(label = " ", searchable = false)
    private final List<RestrictToCafcassHmcts> restrictCheckboxOtherDocuments;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "  ", searchable = false, typeOverride = FieldType.Label)
  private String checkDocumentsConfidentialLabel;
  // ==== end synthesised definition-only fields ====
}
