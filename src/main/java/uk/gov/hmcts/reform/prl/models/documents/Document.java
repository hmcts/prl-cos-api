package uk.gov.hmcts.reform.prl.models.documents;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document {

    @JsonProperty("document_url")
    String documentUrl;
    @JsonProperty("document_binary_url")
    String documentBinaryUrl;
    @JsonProperty("document_filename")
    String documentFileName;
    @JsonProperty("document_hash")
    String documentHash;
    @JsonProperty("category_id")
    String categoryId;
    @JsonProperty("document_creation_date")
    Date documentCreatedOn;

    @JsonCreator
    public Document(@JsonProperty("document_url") String documentUrl,
                    @JsonProperty("document_binary_url") String documentBinaryUrl,
                    @JsonProperty("document_filename") String documentFileName,
                    @JsonProperty("document_hash") String documentHash,
                    @JsonProperty("category_id") String categoryId,
                    @JsonProperty("document_creation_date") Date documentCreatedOn) {
        this.documentUrl = documentUrl;
        this.documentBinaryUrl = documentBinaryUrl;
        this.documentFileName = documentFileName;
        this.documentHash = documentHash;
        this.categoryId = categoryId;
        this.documentCreatedOn = documentCreatedOn;
    }

    public static Document buildFromDocument(uk.gov.hmcts.reform.ccd.document.am.model.Document document) {
        return Document.builder()
            .documentUrl(document.links.self.href)
            .documentBinaryUrl(document.links.binary.href)
            .documentFileName(document.originalDocumentName)
            .documentCreatedOn(document.createdOn)
            .build();
    }


}
