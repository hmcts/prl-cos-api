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
public enum AllocatedJudgeTypeEnum {
    @CCD(label = "Judge")
    @JsonProperty("judge")
    judge("judge", "Judge"),
    @CCD(label = "Legal adviser")
    @JsonProperty("legalAdviser")
    legalAdviser("legalAdviser", "Legal adviser");

    private final String id;
    private final String displayedValue;
}
