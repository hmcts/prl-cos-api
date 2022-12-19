package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.safetyconcerns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Abuse {
    private final String behaviourDetails;
    private final String behaviourStartDate;
    private final YesOrNo isOngoingBehaviour;
    private final YesOrNo seekHelpFromPersonOrAgency;
    private final String seekHelpDetails;
    private final String childrenConcernedAbout;
}

