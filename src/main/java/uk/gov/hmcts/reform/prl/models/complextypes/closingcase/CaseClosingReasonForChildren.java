package uk.gov.hmcts.reform.prl.models.complextypes.closingcase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.closingcase.CaseClosingReasonEnum;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CaseClosingReasonForChildren {
    @CCD(label = "Id")
    private final String childId;
    @CCD(label = " ")
    private final String childName;
    @CCD(label = " ")
    private final CaseClosingReasonEnum caseClosingReason;
}
