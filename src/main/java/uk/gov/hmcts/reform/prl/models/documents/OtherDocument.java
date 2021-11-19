package uk.gov.hmcts.reform.prl.models.documents;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;

@Data
@Builder
public class OtherDocument {

    private final Document file;

    @JsonCreator
    public OtherDocument(Document file) {
        this.file = file;
    }

}
