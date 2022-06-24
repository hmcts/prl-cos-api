package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class FL401OtherProceedingDetails {
    private final YesNoDontKnow hasPrevOrOngoingOtherProceeding;
    private List<Element<FL401Proceedings>> fl401OtherProceedings;
}
