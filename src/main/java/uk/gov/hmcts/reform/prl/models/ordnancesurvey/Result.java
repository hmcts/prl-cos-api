package uk.gov.hmcts.reform.prl.models.ordnancesurvey;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {
    @JsonProperty("DPA")
    private Dpa dpa;
}
