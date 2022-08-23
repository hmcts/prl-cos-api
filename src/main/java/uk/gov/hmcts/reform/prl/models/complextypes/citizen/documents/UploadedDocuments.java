package uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
public class UploadedDocuments {
    private final String parentDocumentType;
    private final String documentType;
    private final String partyName;
    private final String isApplicant;
    private final String uploadedBy;
    private final LocalDate dateCreated;
    private final DocumentDetails documentDetails;
    private final Document citizenDocument;
}
