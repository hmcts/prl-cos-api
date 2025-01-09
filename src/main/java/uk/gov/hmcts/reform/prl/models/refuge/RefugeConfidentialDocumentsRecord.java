package uk.gov.hmcts.reform.prl.models.refuge;

import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.refuge.RefugeConfidentialDocuments;

import java.util.List;

public record RefugeConfidentialDocumentsRecord(
    List<Element<RefugeConfidentialDocuments>> refugeDocuments,
    List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments) {
}
