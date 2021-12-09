package uk.gov.hmcts.reform.prl.models.documents;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MiamDocument {

    private final Document file;

    @JsonCreator
    public MiamDocument(Document file) {
        this.file = file;
    }


}
