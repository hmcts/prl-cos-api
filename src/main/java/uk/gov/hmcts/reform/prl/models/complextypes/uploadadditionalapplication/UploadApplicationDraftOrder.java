package uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DocumentAcknowledge;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class UploadApplicationDraftOrder {
    private final String title;
    private final Document document;
    private final List<DocumentAcknowledge> documentAcknowledge;
    private final YesOrNo documentRelatedToCase;

}
