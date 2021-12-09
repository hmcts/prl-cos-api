package uk.gov.hmcts.reform.prl.models.documents;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class C8Document {

    private final Document file;

    @JsonCreator
    public C8Document(Document file) {
        this.file = file;
    }


}
