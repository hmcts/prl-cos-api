package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder
public class AllegationsOfHarmRevisedChildContact {

    private final YesOrNo newAgreeChildUnsupervisedTime;
    private final YesOrNo newAgreeChildSupervisedTime;
    private final YesOrNo newAgreeChildOtherContact;


}
