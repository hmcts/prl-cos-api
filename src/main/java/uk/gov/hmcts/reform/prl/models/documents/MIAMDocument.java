package uk.gov.hmcts.reform.prl.models.documents;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MIAMDocument {

    private final Document file;

    @JsonCreator
    public MIAMDocument(Document file) {
        this.file = file;
    }


}
