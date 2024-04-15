package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class Document {

    @JsonProperty("document_url")
    String documentUrl;
    @JsonProperty("document_filename")
    String documentFileName;
    @JsonProperty("document_hash")
    String documentHash;

    @JsonCreator
    public Document(@JsonProperty("document_url") String documentUrl,
                    @JsonProperty("document_filename") String documentFileName,
                    @JsonProperty("document_hash") String documentHash) {
        this.documentUrl = documentUrl;
        this.documentFileName = documentFileName;
        this.documentHash = documentHash;
    }

    public static Document buildFromDocument(uk.gov.hmcts.reform.ccd.document.am.model.Document document) {
        return Document.builder()
            .documentUrl(document.links.self.href)
            .documentFileName(document.originalDocumentName)
            .build();
    }

    public static Document buildFromPrlDocument(uk.gov.hmcts.reform.prl.models.documents.Document cafcassDocument) {
        return Document.builder()
            .documentUrl(cafcassDocument.getDocumentUrl())
            .documentFileName(cafcassDocument.getDocumentFileName())
            .build();
    }
}
