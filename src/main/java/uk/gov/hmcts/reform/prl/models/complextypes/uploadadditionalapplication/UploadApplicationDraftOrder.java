package uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DocumentAcknowledge;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
public class UploadApplicationDraftOrder {
    @CCD(label = "Document name", searchable = false)
    private final String title;
    @CCD(label = "Document", searchable = false)
    private final Document document;
    @CCD(label = "Tick to confirm this document is related to this case", searchable = false)
    private final List<DocumentAcknowledge> documentAcknowledge;
    @CCD(
            label = "Document is related to Familyman Case number/Names/CCD",
            showCondition = "documentAcknowledge=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo documentRelatedToCase;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabel;
  // ==== end synthesised definition-only fields ====
}
