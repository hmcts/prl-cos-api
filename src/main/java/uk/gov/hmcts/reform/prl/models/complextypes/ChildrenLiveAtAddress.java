package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class ChildrenLiveAtAddress {
    @CCD(
            label = "*Do you need to keep this information confidential?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo keepChildrenInfoConfidential;
    @CCD(label = "*Child’s full name", searchable = false)
    private final String childFullName;
    @CCD(label = "Child’s age", searchable = false, typeOverride = FieldType.Number)
    private final String childsAge;
    @CCD(
            label = "*Is the respondent also responsible for the child?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
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
