package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;


@Data
@Builder
public class UploadDocuments {

    @JsonProperty("uploadedDocuments")
    private final Document uploadDocuments;

    @JsonCreator
    public UploadDocuments(Document uploadDocuments) {
        this.uploadDocuments = uploadDocuments;
    }
}
