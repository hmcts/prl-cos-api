package uk.gov.hmcts.reform.prl.services.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
class DocumentNode {
    private String documentUrl;
    private String uploadTimestamp;
    private String documentFilename;
    private String documentBinaryUrl;
    private String documentId;
    private JsonNode parent;
    private String documentKey;
    private JsonNode document;
    private List<String> parentKeys;

    public String getParentPath() {
        return String.join(".", parentKeys);
    }
}
