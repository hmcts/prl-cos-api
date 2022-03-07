package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.UploadDocument;

@Data
@Builder
public class fl401UploadSupportDocumentsDetails {

    @JsonProperty("uploadSupportDocuments")
    private final UploadDocument uploadSupportDocuments;

    @JsonCreator
    public fl401UploadSupportDocumentsDetails(UploadDocument uploadSupportDocuments) {
        this.uploadSupportDocuments = uploadSupportDocuments;
    }
}
