package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.UploadDocument;

@Data
@Builder
public class FL401UploadSupportDocumentsDetails {

    @JsonProperty("uploadSupportDocuments")
    private final UploadDocument uploadSupportDocuments;

    @JsonCreator
    public FL401UploadSupportDocumentsDetails(UploadDocument uploadSupportDocuments) {
        this.uploadSupportDocuments = uploadSupportDocuments;
    }
}
