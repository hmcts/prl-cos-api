package uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.refuge;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Builder
@Data
public class RefugeCase {
    private final YesOrNo isRefugeCase;
}
