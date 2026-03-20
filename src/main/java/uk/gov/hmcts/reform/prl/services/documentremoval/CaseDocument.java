package uk.gov.hmcts.reform.prl.services.documentremoval;

import java.time.LocalDateTime;

public record CaseDocument(String documentId, String filename, LocalDateTime uploadTimestamp) {
}
