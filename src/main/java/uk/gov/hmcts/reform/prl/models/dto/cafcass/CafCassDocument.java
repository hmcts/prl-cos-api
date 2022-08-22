package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CafCassDocument {

    @JsonProperty("document_id")
    String documentId;
    @JsonProperty("document_filename")
    String documentFileName;

    public static CafCassDocument buildFromDocument(uk.gov.hmcts.reform.ccd.document.am.model.Document document) {
        return CafCassDocument.builder()
            .documentId(document.links.self.href)
            .documentFileName(document.originalDocumentName)
            .build();
    }


}
