package uk.gov.hmcts.reform.prl.enums.citizen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UrgentHearingReasonEnum {
    riskofsafety("Risk to my safety or the children's safety"),
    riskofchildabduction("Risk that the children will be abducted"),
    overseaslegalproceeding("Legal proceedings taking place overseas"),
    otherrisks("Other risks");

    public String getDisplayedValue() {
        return displayedValue;
    }

    private final String displayedValue;


}
