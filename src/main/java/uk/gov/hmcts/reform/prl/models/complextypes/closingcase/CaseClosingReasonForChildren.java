package uk.gov.hmcts.reform.prl.models.complextypes.closingcase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.closingcase.CaseClosingReasonEnum;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CaseClosingReasonForChildren {
    private final String childId;
    private final String childName;
    private final CaseClosingReasonEnum caseClosingReason;
}
