package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoCrossExaminationSittingBeforeEnum {

    @JsonProperty("judge")
    judge("judge", "Judge"),
    @JsonProperty("magistrates")
    magistrates("magistrates", "Magistrates");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoCrossExaminationSittingBeforeEnum getValue(String key) {
        return SdoCrossExaminationSittingBeforeEnum.valueOf(key);
    }
}
