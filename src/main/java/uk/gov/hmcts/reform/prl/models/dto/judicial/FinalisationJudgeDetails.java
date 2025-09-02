package uk.gov.hmcts.reform.prl.models.dto.judicial;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinalisationJudgeDetails {

    @JsonCreator
    public FinalisationJudgeDetails(@JsonProperty("appointment") String appointment) {
        this.appointment = appointment;
    }

    //Tier of judge
    @JsonProperty("appointment")
    private final String appointment;

    // @JsonProperty("tierOfJudiciary")
    // private final TierOfJudiciaryEnum tierOfJudiciary;
}
