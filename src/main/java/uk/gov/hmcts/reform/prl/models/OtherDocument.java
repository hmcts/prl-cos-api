package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtherDocument {

    private final Document file;

    @JsonCreator
    public OtherDocument(Document file) {
        this.file = file;
    }

}
