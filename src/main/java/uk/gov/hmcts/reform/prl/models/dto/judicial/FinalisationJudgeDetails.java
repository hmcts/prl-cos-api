package uk.gov.hmcts.reform.prl.models.dto.judicial;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinalisationJudgeDetails {


    //Tier of judge
    @JsonProperty("appointment")
    private final String appointment;

    // @JsonProperty("tierOfJudiciary")
    // private final TierOfJudiciaryEnum tierOfJudiciary;
}
