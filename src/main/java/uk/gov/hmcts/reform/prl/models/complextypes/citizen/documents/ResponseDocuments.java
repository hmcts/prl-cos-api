package uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentDetails;

@Data
@Builder(toBuilder = true)
public class ResponseDocuments {
    @CCD(label = "Responding party name", searchable = false)
    private final String partyName;
    @CCD(label = "Submitted by", searchable = false)
    private final String createdBy;
    @CCD(label = "Submitted on", searchable = false)
    private final LocalDate dateCreated;
    @CCD(label = "Document", searchable = false)
    private final Document citizenDocument;
    @CCD(label = "C8 document english", categoryID = "confidential", searchable = false)
    private final Document respondentC8Document;
    @CCD(label = "C8 document welsh", categoryID = "confidential", searchable = false)
    private final Document respondentC8DocumentWelsh;
    @CCD(label = "Date created", showCondition = "dateCreated=\"DO_NOT_SHOW\"", searchable = false)
    private final LocalDateTime dateTimeCreated;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false)
  private DocumentDetails documentDetails;
  // ==== end synthesised definition-only fields ====
}
