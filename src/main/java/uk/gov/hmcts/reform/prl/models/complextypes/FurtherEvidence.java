package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.FurtherEvidenceDocumentType;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder
public class FurtherEvidence {
    private final FurtherEvidenceDocumentType typeOfDocumentFurtherEvidence;
    private final Document documentFurtherEvidence;
    private final List<RestrictToCafcassHmcts> restrictCheckboxFurtherEvidence;
}
