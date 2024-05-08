package uk.gov.hmcts.reform.prl.models.documents;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Date;

@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    @JsonProperty("upload_timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS][.SSSSSS]")
    LocalDateTime uploadTimeStamp;



    @JsonCreator
    public Document(@JsonProperty("document_url") String documentUrl,
                    @JsonProperty("document_binary_url") String documentBinaryUrl,
                    @JsonProperty("document_filename") String documentFileName,
                    @JsonProperty("document_hash") String documentHash,
                    @JsonProperty("category_id") String categoryId,
                    Date documentCreatedOn, LocalDateTime uploadTimeStamp) {
        this.documentUrl = documentUrl;
        this.documentBinaryUrl = documentBinaryUrl;
        this.documentFileName = documentFileName;
        this.documentHash = documentHash;
        this.categoryId = categoryId;
        this.documentCreatedOn = documentCreatedOn;
        this.uploadTimeStamp = uploadTimeStamp;
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
