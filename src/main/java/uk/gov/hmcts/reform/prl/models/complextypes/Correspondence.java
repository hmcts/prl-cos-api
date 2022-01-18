package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.FurtherEvidenceDocumentType;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.models.documents.CorrespondenceDocument;
import uk.gov.hmcts.reform.prl.models.documents.FurtherEvidenceDocument;

import java.util.List;

@Data
@Builder
public class Correspondence {
    @JsonProperty("documentName")
    private final String documentName;
    @JsonProperty("notes")
    private final String notes;
    private final CorrespondenceDocument documentCorrespondence;
    private final List<RestrictToCafcassHmcts> restrictCheckboxCorrespondence;
}
