package uk.gov.hmcts.reform.prl.models.dto.judicial;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.TierOfJudiciaryEnum;


@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinalisationJudgeDetails {

    @JsonProperty("tierOfJudiciary")
    private final TierOfJudiciaryEnum tierOfJudiciary;
}
