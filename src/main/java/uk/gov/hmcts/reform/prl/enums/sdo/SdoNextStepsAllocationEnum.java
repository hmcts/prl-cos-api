package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoNextStepsAllocationEnum {

    @JsonProperty("magistrates")
    magistrates("magistrates", "Magistrates"),
    @JsonProperty("circuitJudge")
    circuitJudge("circuitJudge", "Circuit judge"),
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
