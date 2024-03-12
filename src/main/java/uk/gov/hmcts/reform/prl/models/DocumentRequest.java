package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentRequest {

    @JsonProperty("isWelsh")
    private boolean isWelsh;
}
