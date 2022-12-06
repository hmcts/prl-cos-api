package uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;

@Data
@Builder(toBuilder = true)
public class ServeOrderAdditionalDocument {

    private final Document serveOrderDocument;
}
