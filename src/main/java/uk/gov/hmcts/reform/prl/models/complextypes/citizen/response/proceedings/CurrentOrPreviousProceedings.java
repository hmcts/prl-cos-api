package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CurrentOrPreviousProceedings {
    private final YesNoDontKnow haveChildrenBeenInvolvedInCourtCase;
    private final YesOrNo courtOrderMadeForProtection;
    private List<Proceedings> proceedingsList;
}
