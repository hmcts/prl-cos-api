package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Builder
@Data
public class HomeChild {

    private final YesOrNo keepChildrenInfoConfidential;
    private final String childFullName;
    private final String childsAge;
    private final String isRespondentResponsibleForChild;
}
