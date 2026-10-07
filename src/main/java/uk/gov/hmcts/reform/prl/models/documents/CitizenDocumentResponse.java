package uk.gov.hmcts.reform.prl.models.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CitizenDocumentResponse {
    private String status;
    private FileUploadSuccess success;
    private Document file;
}
