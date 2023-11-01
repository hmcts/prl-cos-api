package uk.gov.hmcts.reform.prl.enums.gatekeeping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum AllocatedJudgeTypeEnum {
    @JsonProperty("judge")
    judge("judge", "Judge"),
    @JsonProperty("legalAdviser")
    legalAdviser("legalAdviser", "Legal adviser");

    private final String id;
    private final String displayedValue;
}
