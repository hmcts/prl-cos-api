package uk.gov.hmcts.reform.prl.models.ordnancesurvey;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Result {
    @JsonProperty("DPA")
    private Dpa dpa;
}
