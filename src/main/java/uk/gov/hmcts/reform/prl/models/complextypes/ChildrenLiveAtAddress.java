package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder
public class ChildrenLiveAtAddress {
    private final YesOrNo keepChildrenInfoConfidential;
    private final String childFullName;
    private final String childsAge;
    private final YesOrNo isRespondentResponsibleForChild;

    @JsonCreator
    public ChildrenLiveAtAddress(YesOrNo keepChildrenInfoConfidential, String childFullName,
                                 String childsAge, YesOrNo isRespondentResponsisbleYesNo) {
        this.keepChildrenInfoConfidential = keepChildrenInfoConfidential;
        this.childFullName = childFullName;
        this.childsAge = childsAge;
        this.isRespondentResponsibleForChild = isRespondentResponsisbleYesNo;
    }
}
