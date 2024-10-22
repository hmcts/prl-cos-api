package uk.gov.hmcts.reform.prl.models.refuge;

import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

public record RefugeConfidentialDocumentsRecord(
    List<Element<uk.gov.hmcts.reform.prl.models.complextypes.refuge.RefugeConfidentialDocuments>> refugeDocuments,
    List<Element<uk.gov.hmcts.reform.prl.models.complextypes.refuge.RefugeConfidentialDocuments>> historicalRefugeDocuments) {
}
