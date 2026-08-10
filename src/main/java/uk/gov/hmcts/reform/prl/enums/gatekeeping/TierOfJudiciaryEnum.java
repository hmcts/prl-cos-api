package uk.gov.hmcts.reform.prl.enums.gatekeeping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum TierOfJudiciaryEnum {
    @CCD(label = "Magistrates")
    @JsonProperty("magistrates")
    magistrates("magistrates", "Magistrates"),
    @CCD(label = "District Judge")
    @JsonProperty("districtJudge")
    districtJudge("districtJudge", "District Judge"),
    @CCD(label = "Circuit Judge")
    @JsonProperty("circuitJudge")
    circuitJudge("circuitJudge", "Circuit Judge"),
    @CCD(label = "High Court Judge")
    @JsonProperty("highCourtJudge")
    highCourtJudge("highCourtJudge", "High Court Judge");

    private final String id;
    private final String displayedValue;
}
