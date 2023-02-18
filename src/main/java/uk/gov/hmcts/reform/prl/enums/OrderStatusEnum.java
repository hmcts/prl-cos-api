package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum OrderStatusEnum {

    @JsonProperty("createdByCA")
    createdByCA("createdByCA", "Created by CA"),
    @JsonProperty("createdByJudge")
    createdByJudge("createdByJudge", "Created by Judge"),
    @JsonProperty("draftedByLR")
    draftedByLR("draftedByLR", "Drafted by LR"),
    @JsonProperty("reviewedByJudge")
    reviewedByJudge("reviewedByJudge", "Reviewed by Judge"),
    @JsonProperty("reviewedByManager")
    reviewedByManager("reviewedByManager", "Reviewed by Manager"),
    @JsonProperty("reviewedByCA")
    reviewedByCA("reviewedByCA", "Reviewed by CA");

    private final String id;
    private final String displayedValue;


    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static OrderStatusEnum getValue(String key) {
        return OrderStatusEnum.valueOf(key);
    }


}
