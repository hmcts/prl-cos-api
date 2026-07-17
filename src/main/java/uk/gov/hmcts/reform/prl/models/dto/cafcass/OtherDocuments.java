package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OtherDocuments {
    @CCD(
            label = "Document name",
            hint = "Add a descriptive name. For example, \"letters from social workers\" or \"letter from doctor\"",
            searchable = false
    )
    @JsonProperty("documentName")
    private String documentName;
    @CCD(label = "Notes", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("notes")
    private String notes;
    @CCD(label = "Upload document", categoryID = "anyOtherDoc", searchable = false)
    private Document documentOther;
    @CCD(label = "Choose a further evidence document type", searchable = false)
    private DocTypeOtherDocumentsEnum documentTypeOther;
    @CCD(label = " ", searchable = false)
    private List<RestrictToCafcassHmcts> restrictCheckboxOtherDocuments;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "  ", searchable = false, typeOverride = FieldType.Label)
  private String checkDocumentsConfidentialLabel;
  // ==== end synthesised definition-only fields ====
}
