package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder
@AllArgsConstructor
public class WithdrawApplication {
    private final YesOrNo withDrawApplication;
    private final String withDrawApplicationReason;
}
