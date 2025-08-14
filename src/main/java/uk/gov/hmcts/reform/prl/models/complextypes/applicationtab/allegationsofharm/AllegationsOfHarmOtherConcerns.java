package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder
public class AllegationsOfHarmOtherConcerns {

    private final YesOrNo allegationsOfHarmOtherConcerns;
    private final String allegationsOfHarmOtherConcernsDetails;
    private final String allegationsOfHarmOtherConcernsCourtActions;
    private final YesOrNo agreeChildUnsupervisedTime;
    private final YesOrNo agreeChildSupervisedTime;
    private final YesOrNo agreeChildOtherContact;


}
