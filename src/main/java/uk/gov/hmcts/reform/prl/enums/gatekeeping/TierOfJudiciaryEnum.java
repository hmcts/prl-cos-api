package uk.gov.hmcts.reform.prl.enums.gatekeeping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum TierOfJudiciaryEnum {
    @JsonProperty("magistrates")
    magistrates("magistrates", "Magistrates"),
    @JsonProperty("districtJudge")
    districtJudge("districtJudge", "District Judge"),
    @JsonProperty("circuitJudge")
    circuitJudge("circuitJudge", "Circuit Judge"),
    @JsonProperty("highCourtJudge")
    highCourtJudge("highCourtJudge", "High Court Judge");

    private final String id;
    private final String displayedValue;
}
