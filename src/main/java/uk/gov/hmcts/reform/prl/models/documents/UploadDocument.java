package uk.gov.hmcts.reform.prl.models.documents;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadDocument {

    private final Document file;

    @JsonCreator
    public UploadDocument(Document file) {
        this.file = file;
    }
}
