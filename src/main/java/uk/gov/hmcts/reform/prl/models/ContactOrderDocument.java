package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Document;

@Data
@Builder
public class ContactOrderDocument {

    private final Document file;

    @JsonCreator
    public ContactOrderDocument(Document file) {
        this.file = file;
    }


}
