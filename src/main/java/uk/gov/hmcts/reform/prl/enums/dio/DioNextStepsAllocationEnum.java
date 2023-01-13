package uk.gov.hmcts.reform.prl.enums.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DioNextStepsAllocationEnum {

    @JsonProperty("magistrates")
    magistrates("magistrates", "Magistrates"),
    @JsonProperty("districtJudge")
    districtJudge("districtJudge", "District judge"),
    @JsonProperty("circuitJudge")
    circuitJudge("circuitJudge", "Circuit judge");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DioNextStepsAllocationEnum getValue(String key) {
        return DioNextStepsAllocationEnum.valueOf(key);
    }

}
