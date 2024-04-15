package uk.gov.hmcts.reform.prl.models.complextypes.respondentsolicitor.documents;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class RespondentDocs {
    private ResponseDocuments c1aDocument;
    private ResponseDocuments c7Document;
    private ResponseDocuments c7WelshDocument;
    private List<Element<ResponseDocuments>> otherDocuments;
}
