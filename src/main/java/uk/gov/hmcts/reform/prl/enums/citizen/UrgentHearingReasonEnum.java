package uk.gov.hmcts.reform.prl.enums.citizen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UrgentHearingReasonEnum {
    riskOfSafety("Risk to my safety or the children's safety"),
    riskOfChildAbduction("Risk that the children will be abducted"),
    overseasLegalProceeding("Legal proceedings taking place overseas"),
    otherRisks("Other risks");

    public String getDisplayedValue() {
        return displayedValue;
    }

    private final String displayedValue;


}
