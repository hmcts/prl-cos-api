package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum OrderStatusEnum {

    createdByCA("createdByCA", "Created by CA"),
    createdByJudge("createdByJudge", "Created by Judge"),
    draftedByLR("draftedByLR", "Drafted by LR"),
    reviewedByJudge("reviewedByJudge", "Reviewed by Judge"),
    reviewedByManager("reviewedByManager", "Reviewed by Manager"),
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
