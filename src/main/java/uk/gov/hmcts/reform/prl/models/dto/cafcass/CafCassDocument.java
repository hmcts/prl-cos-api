package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class CafCassDocument {

    @JsonProperty("document_url")
    private String documentUrl;
    @JsonProperty("document_filename")
    private String documentFileName;
    @JsonProperty("document_id")
    private String documentId;

    public static CafCassDocument buildFromDocument(uk.gov.hmcts.reform.ccd.document.am.model.Document document) {
        return CafCassDocument.builder()
            .documentUrl(document.links.self.href)
            .documentFileName(document.originalDocumentName)
            .build();
    }


}
