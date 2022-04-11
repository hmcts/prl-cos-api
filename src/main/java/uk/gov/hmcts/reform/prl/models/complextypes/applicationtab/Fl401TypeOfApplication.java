package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Builder
@Data
public class Fl401TypeOfApplication {
    private final String ordersApplyingFor;
    private final YesOrNo isLinkedToChildArrangementApplication;
    private final String caCaseNumber;

}
