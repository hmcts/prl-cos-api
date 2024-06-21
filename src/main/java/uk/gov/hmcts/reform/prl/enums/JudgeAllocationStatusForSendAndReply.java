package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum JudgeAllocationStatusForSendAndReply {
    @JsonProperty("judge_already_allocated")
    judge_already_allocated("judge_already_allocated", "judge_already_allocated"),
    @JsonProperty("snr_assigned")
    snr_assigned("snr_assigned", "snr_assigned"),

    @JsonProperty("unallocated")
    unallocated("unallocated", "unallocated"),

    @JsonProperty("judge_not_selected")
    judge_not_selected("judge_not_selected", "judge_not_selected");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static JudgeAllocationStatusForSendAndReply getValue(String key) {
        return JudgeAllocationStatusForSendAndReply.valueOf(key);
    }

    public static JudgeAllocationStatusForSendAndReply fromValue(String value) {
        return Arrays.stream(values())
            .filter(contactPref -> contactPref.getDisplayedValue().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown JudgeAllocationStatusForSendAndReply: " + value));
    }

}
