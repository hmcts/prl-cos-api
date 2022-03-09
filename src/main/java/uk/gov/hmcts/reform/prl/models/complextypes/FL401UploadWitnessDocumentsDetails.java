package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.UploadDocument;

@Data
@Builder
public class FL401UploadWitnessDocumentsDetails {

    @JsonProperty("uploadWitnessDocuments")
    private final UploadDocument uploadWitnessDocuments;

    @JsonCreator
    public FL401UploadWitnessDocumentsDetails(UploadDocument uploadWitnessDocuments) {
        this.uploadWitnessDocuments = uploadWitnessDocuments;
    }
}
