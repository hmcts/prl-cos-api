package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class RespondentOtherConcerns {

    private final String ordersRespondentWantFromCourt;
    private final YesOrNo childSpendingUnsupervisedTime;
    private final YesOrNo childSpendingSupervisedTime;
    private final YesOrNo childHavingOtherFormOfContact;
}
