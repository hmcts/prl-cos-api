package uk.gov.hmcts.reform.prl.models.complextypes.citizen.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;



@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CitizenFlags {

    private final YesOrNo isApplicationViewed;
    private final YesOrNo isAllegationOfHarmViewed;
    private final YesOrNo isAllDocumentsViewed;
    private final YesOrNo isResponseInitiated;
    private YesOrNo isApplicationToBeServed;
    private YesOrNo isStatementOfServiceProvided;
}
