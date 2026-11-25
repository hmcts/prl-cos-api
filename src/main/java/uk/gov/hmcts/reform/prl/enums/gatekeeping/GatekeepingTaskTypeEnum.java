package uk.gov.hmcts.reform.prl.enums.gatekeeping;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum GatekeepingTaskTypeEnum {
    JUDGE("JUDGE", "Judge task"),
    LEGAL_ADVISER("LEGAL_ADVISER", "Legal adviser task"),
    JUDGE_OR_LEGAL_ADVISER("JUDGE_OR_LEGAL_ADVISER", "Judge or legal adviser task");

    private final String id;
    private final String displayedValue;
}

