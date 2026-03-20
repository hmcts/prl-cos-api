package uk.gov.hmcts.reform.prl.services.documentremoval;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder
public class DocumentInstances {
    private Document document;
    private List<String> instances;
}
