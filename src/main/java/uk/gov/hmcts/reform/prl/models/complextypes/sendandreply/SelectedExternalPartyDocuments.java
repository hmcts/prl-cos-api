package uk.gov.hmcts.reform.prl.models.complextypes.sendandreply;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class SelectedExternalPartyDocuments {

    private String selectedDocumentCode;

    private String selectedDocumentValue;

}
