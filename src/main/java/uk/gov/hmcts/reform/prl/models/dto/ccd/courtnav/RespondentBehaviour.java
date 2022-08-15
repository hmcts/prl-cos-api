package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsApplicantEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsChildrenEnum;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class RespondentBehaviour {

    private final boolean applyingForMonMolestationOrder;
    private final List<BehaviourTowardsApplicantEnum> stopBehaviourTowardsApplicant;
    private final List<BehaviourTowardsChildrenEnum> stopBehaviourTowardsChildren;
    private final String stopBehaviourAnythingElse;
}
