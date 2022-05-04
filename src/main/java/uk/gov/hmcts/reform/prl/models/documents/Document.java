package uk.gov.hmcts.reform.prl.models.documents;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Document {

    @JsonProperty("document_url")
    String documentUrl;
    @JsonProperty("document_binary_url")
    String documentBinaryUrl;
    @JsonProperty("document_filename")
    String documentFileName;
    @JsonProperty("document_hash")
    String documentHash;

    @JsonCreator
    public Document(@JsonProperty("document_url") String documentUrl,
                    @JsonProperty("document_binary_url") String documentBinaryUrl,
                    @JsonProperty("document_filename") String documentFileName,
                    @JsonProperty("document_hash") String documentHash) {
        this.documentUrl = documentUrl;
        this.documentBinaryUrl = documentBinaryUrl;
        this.documentFileName = documentFileName;
        this.documentHash = documentHash;
    }
}
