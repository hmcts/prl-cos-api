package uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class ResponseDocuments {
    private final String partyName;
    private final String createdBy;
    private final LocalDate dateCreated;
    private final Document citizenDocument;
    private final Document respondentC8Document;
    private final Document respondentC8DocumentWelsh;
    private final LocalDateTime dateTimeCreated;
}
