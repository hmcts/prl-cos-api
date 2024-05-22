package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum OrderStatusEnum {
    draftedByLR("draftedByLR", "Drafted by Solicitor", 1),
    createdByCA("createdByCA", "Created by Admin", 2),
    reviewedByCA("reviewedByCA", "Reviewed by Admin", 3),
    reviewedByManager("reviewedByManager", "Reviewed by Manager", 4),
    createdByJudge("createdByJudge", "Created by Judge", 5),
    reviewedByJudge("reviewedByJudge", "Reviewed by Judge", 6),
    rejectedByJudge("rejectedByJudge", "Rejected by Judge", 7);


    private final String id;
    private final String displayedValue;

    private final int priority;


    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonValue
    public int getPriority() {
        return priority;
    }

    @JsonCreator
    public static OrderStatusEnum getValue(String key) {
        return OrderStatusEnum.valueOf(key);
    }

    public static OrderStatusEnum fromDisplayedValue(String text) {
        for (OrderStatusEnum orderStatusEnum : OrderStatusEnum.values()) {
            if (orderStatusEnum.displayedValue.equalsIgnoreCase(text)) {
                return orderStatusEnum;
            }
        }
        return null;
    }


}
