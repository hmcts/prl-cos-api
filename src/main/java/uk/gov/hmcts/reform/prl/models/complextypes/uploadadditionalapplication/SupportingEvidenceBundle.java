package uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DocumentAcknowledge;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
public class SupportingEvidenceBundle {

    @CCD(label = "Document name", searchable = false)
    @JsonProperty("name")
    private final String name;
    @CCD(label = "Notes", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("notes")
    private final String notes;
    @CCD(label = "Document", searchable = false)
    @JsonProperty("document")
    private final Document document;
    @CCD(label = "Tick to confirm this document is related to this case", searchable = false)
    @JsonProperty("documentAcknowledge")
    private final List<DocumentAcknowledge> documentAcknowledge;
    @CCD(label = "Date and time uploaded", showCondition = "name=\"DO NOT SHOW\"", searchable = false)
    @JsonProperty("dateTimeUploaded")
    private LocalDateTime dateTimeUploaded;
    @CCD(label = "Uploaded by", showCondition = "name=\"DO NOT SHOW\"", searchable = false)
    @JsonProperty("uploadedBy")
    private String uploadedBy;
    @CCD(
            label = "Document is related to Familyman Case number/Names/CCD",
            showCondition = "documentAcknowledge=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("documentRelatedToCase")
    private final YesOrNo documentRelatedToCase;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Date and time received",
          hint = "For example, 31 3 2016  2 30 00",
          showCondition = "name=\"DO NOT SHOW\"",
          searchable = false
  )
  private java.time.LocalDateTime dateTimeReceived;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabel;
  // ==== end synthesised definition-only fields ====
}
