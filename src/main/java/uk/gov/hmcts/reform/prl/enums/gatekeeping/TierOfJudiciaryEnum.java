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
    MAGISTRATES("magistrates", "Magistrates"),
    @JsonProperty("districtJudge")
    DISTRICT_JUDGE("districtJudge", "District Judge"),
    @JsonProperty("circuitJudge")
    CIRCUIT_JUDGE("circuitJudge", "Circuit Judge"),
    @JsonProperty("highCourtJudge")
    HIGHCOURT_JUDGE("highCourtJudge", "High Court Judge");

    private final String id;
    private final String displayedValue;
}
