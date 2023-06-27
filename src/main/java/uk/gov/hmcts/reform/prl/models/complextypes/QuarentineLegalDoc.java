package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder
public class QuarentineLegalDoc {
    private final String documentName;
    private final String notes;
    private final Document document;
    private final String documentType;
    private final String category;
    private final List<RestrictToCafcassHmcts> restrictCheckboxCorrespondence;
}
