package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.*;
import uk.gov.hmcts.reform.prl.models.documents.FurtherEvidenceDocument;

@Data
@Builder
public class FurtherEvidence {
    private final FurtherEvidenceDocumentType typeOfDocumentFurtherEvidence;
    private final FurtherEvidenceDocument documentFurtherEvidence;
    private final RestrictToCafcassHmcts restrictCheckboxFurtherEvidence;
}
