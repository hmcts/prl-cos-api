package uk.gov.hmcts.reform.prl.enums.gatekeeping;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum WhoToSendToGatekeeperTypeEnum {
    SEND_TO_A_SPECIFIC_JUDGE("SEND_TO_A_SPECIFIC_JUDGE", "Send to a specific judge"),
    SEND_TO_A_SPECIFIC_LEGAL_ADVISER("SEND_TO_A_SPECIFIC_LEGAL_ADVISER", "Send to a specific legal adviser"),
    SEND_TO_ANY_JUDGE_OR_LEGAL_ADVISER("SEND_TO_ANY_JUDGE_OR_LEGAL_ADVISER", "Send to any judge or legal adviser");

    private final String id;
    private final String displayedValue;
}

