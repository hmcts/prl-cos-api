package uk.gov.hmcts.reform.prl.courtnav.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsApplicantEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsChildrenEnum;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;

import java.util.stream.Collectors;
import javax.json.JsonObject;


@Component
public class RespondentBehaviourMapper {

    private JsonObject map(CourtNavCaseData courtNavCaseData) {

        String behaviourTowardsApplicant = null;

        if (courtNavCaseData.getStopBehaviourTowardsApplicant() != null && !courtNavCaseData.getStopBehaviourTowardsApplicant().isEmpty()) {
            behaviourTowardsApplicant = courtNavCaseData.getStopBehaviourTowardsApplicant()
                .stream()
                .map(BehaviourTowardsApplicantEnum::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        String behaviourTowardsChildren = null;

        if (courtNavCaseData.getStopBehaviourTowardsChildren() != null && !courtNavCaseData.getStopBehaviourTowardsChildren().isEmpty()) {
            behaviourTowardsChildren = courtNavCaseData.getStopBehaviourTowardsChildren()
                .stream()
                .map(BehaviourTowardsChildrenEnum::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        return new NullAwareJsonObjectBuilder()
            .add("applicantWantToStopFromRespondentDoing", behaviourTowardsApplicant)
            .add("applicantWantToStopFromRespondentDoingToChild", behaviourTowardsChildren)
            .add("otherReasonApplicantWantToStopFromRespondentDoing", courtNavCaseData.getStopBehaviourAnythingElse())
            .build();
    }
}
