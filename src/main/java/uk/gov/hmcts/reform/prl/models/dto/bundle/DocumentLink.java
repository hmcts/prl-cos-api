package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class DocumentLink {
    @JsonProperty("document_url")
    private String documentUrl;
    @JsonProperty("document_binary_url")
    private String documentBinaryUrl;
    @JsonProperty("document_filename")
    private String documentFilename;
    @JsonProperty("document_hash")
    private String documentHash;

    @JsonCreator
    public DocumentLink(@JsonProperty("document_url") String documentUrl,
                        @JsonProperty("document_binary_url") String documentBinaryUrl,
                        @JsonProperty("document_filename") String documentFilename,
                        @JsonProperty("document_hash") String documentHash) {
        this.documentUrl = documentUrl;
        this.documentBinaryUrl = documentBinaryUrl;
        this.documentFilename = documentFilename;
        this.documentHash = documentHash;
    }
}
