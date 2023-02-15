package uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;

@Data
@Builder
public class ResponseDocuments {
    private final String partyName;
    private final String createdBy;
    private final LocalDate dateCreated;
    private final Document citizenDocument;
}
