package uk.gov.hmcts.reform.prl.models.cafcass;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CafcassDocumentInfo {
    private String url;
    private String fileName;
    private String documentId;
    private String binaryUrl;
}