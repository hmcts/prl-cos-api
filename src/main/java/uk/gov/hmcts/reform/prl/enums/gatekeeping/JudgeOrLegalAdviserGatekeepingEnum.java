package uk.gov.hmcts.reform.prl.enums.gatekeeping;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum JudgeOrLegalAdviserGatekeepingEnum {
    SEND_TO_A_JUDGE("SEND_TO_A_JUDGE", "Send to a judge"),
    SEND_TO_A_LEGAL_ADVISER("SEND_TO_A_LEGAL_ADVISER", "Send to a legal adviser");

    private final String id;
    private final String displayedValue;
}

