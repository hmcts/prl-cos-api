package uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;

@Data
@Builder
public class ResponseDocuments {
    private final String parentDocumentType;
    private final String documentType;
    private final String partyName;
    private final String uploadedBy;
    private final LocalDate dateCreated;
    private final DocumentDetails documentDetails;
    private final Document citizenDocument;
}
