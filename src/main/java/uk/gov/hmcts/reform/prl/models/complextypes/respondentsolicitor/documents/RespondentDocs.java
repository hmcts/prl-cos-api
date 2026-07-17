package uk.gov.hmcts.reform.prl.models.complextypes.respondentsolicitor.documents;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
public class RespondentDocs {
    @CCD(label = "C1A document", searchable = false)
    private ResponseDocuments c1aDocument;
    @CCD(label = "C7 document", searchable = false)
    private ResponseDocuments c7Document;
    @CCD(label = "C7 welsh document", searchable = false)
    private ResponseDocuments c7WelshDocument;
    @CCD(label = "Other documents", searchable = false)
    private List<Element<ResponseDocuments>> otherDocuments;
}
