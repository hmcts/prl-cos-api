package uk.gov.hmcts.reform.prl.models.complextypes.refuge;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.DocumentDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefugeConfidentialDocuments {
    @CCD(label = "Party Type", searchable = false)
    private final String partyType;
    @CCD(label = "Party name", searchable = false)
    private final String partyName;
    @CCD(label = " ", searchable = false)
    private final DocumentDetails documentDetails;
    @CCD(label = "Document", categoryID = "confidential", searchable = false)
    private final Document document;
}
