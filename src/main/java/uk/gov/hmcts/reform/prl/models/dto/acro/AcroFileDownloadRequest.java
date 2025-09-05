package uk.gov.hmcts.reform.prl.models.dto.acro;

import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;

@Data
public class AcroFileDownloadRequest {

    private final String fileName;
    private final Document orderDocument;
    private final Document orderDocumentWelsh;
}
