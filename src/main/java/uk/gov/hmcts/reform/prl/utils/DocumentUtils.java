package uk.gov.hmcts.reform.prl.utils;

import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;

public class DocumentUtils {

    public static GeneratedDocumentInfo toGeneratedDocumentInfo(Document document) {
        return GeneratedDocumentInfo.builder()
            .url(document.getDocumentUrl())
            .binaryUrl(document.getDocumentBinaryUrl())
            .hashToken(document.getDocumentHash())
            .build();
    }

}
