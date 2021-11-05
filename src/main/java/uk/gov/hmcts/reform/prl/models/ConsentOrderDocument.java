package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Document;

@Data
@Builder
public class ConsentOrderDocument {

    private final Document file;

    @JsonCreator
    public ConsentOrderDocument(Document file) {
        this.file = file;
    }


}
