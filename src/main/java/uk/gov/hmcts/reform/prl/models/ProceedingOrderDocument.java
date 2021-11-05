package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProceedingOrderDocument {

    private final Document file;

    @JsonCreator
    public ProceedingOrderDocument(Document file) {
        this.file = file;
    }

}
