package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.UploadDocument;


@Data
@Builder
public class UploadDocuments {

    @JsonProperty("uploadDocuments")
    private final UploadDocument uploadDocuments;

    @JsonCreator
    public UploadDocuments(UploadDocument uploadDocuments) {
        this.uploadDocuments = uploadDocuments;
    }
}
