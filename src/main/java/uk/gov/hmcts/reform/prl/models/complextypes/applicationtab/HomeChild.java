package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ChildInfo", generate = true)
@Builder
@Data
public class HomeChild {

    @CCD(
            label = "Do you need to keep this information confidential?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo keepChildrenInfoConfidential;
    @CCD(label = "Child’s full name:", searchable = false)
    private final String childFullName;
    @CCD(label = "Child’s age:", searchable = false)
    private final String childsAge;
    @CCD(label = "Is the respondent also responsible for the child?", searchable = false)
    private final String isRespondentResponsibleForChild;
}
