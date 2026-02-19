package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.mapstruct.Mapper;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface RespondentBehaviourMapper {

    default RespondentBehaviour map(CourtNavFl401 source) {
        List<FL401OrderTypeEnum> orderTypes = source.getFl401().getSituation().getOrdersAppliedFor();
        if (orderTypes == null || !orderTypes.contains(FL401OrderTypeEnum.nonMolestationOrder)) {
            return null;
        }

        return RespondentBehaviour.builder()
            .applicantWantToStopFromRespondentDoing(mapBehaviourTowardsApplicant(source))
            .applicantWantToStopFromRespondentDoingToChild(mapBehaviourTowardsChildren(source))
            .otherReasonApplicantWantToStopFromRespondentDoing(
                source.getFl401().getRespondentBehaviour().getStopBehaviourAnythingElse())
            .build();
    }

    private List<ApplicantStopFromRespondentDoingEnum> mapBehaviourTowardsApplicant(CourtNavFl401 source) {
        return Optional.ofNullable(source.getFl401().getRespondentBehaviour().getStopBehaviourTowardsApplicant())
            .orElse(Collections.emptyList())
            .stream()
            .map(value -> ApplicantStopFromRespondentDoingEnum.getDisplayedValueFromEnumString(value.toString()))
            .toList();
    }

    private List<ApplicantStopFromRespondentDoingToChildEnum> mapBehaviourTowardsChildren(CourtNavFl401 source) {
        return Optional.ofNullable(source.getFl401().getRespondentBehaviour().getStopBehaviourTowardsChildren())
            .orElse(Collections.emptyList())
            .stream()
            .map(value -> ApplicantStopFromRespondentDoingToChildEnum.getDisplayedValueFromEnumString(value.toString()))
            .toList();
    }
}

