package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoNextStepsAllocationEnum {

    @CCD(label = "Magistrates")
    @JsonProperty("magistrates")
    magistrates("magistrates", "Magistrates"),
    @CCD(label = "Circuit judge")
    @JsonProperty("circuitJudge")
    circuitJudge("circuitJudge", "Circuit judge"),
    @CCD(label = "District judge")
    @JsonProperty("districtJudge")
    districtJudge("districtJudge", "District judge");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoNextStepsAllocationEnum getValue(String key) {
        return SdoNextStepsAllocationEnum.valueOf(key);
    }
}
