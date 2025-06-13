package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.mapstruct.Mapper;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsApplicantEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsChildrenEnum;

import java.util.ArrayList;
import java.util.List;

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
        List<BehaviourTowardsApplicantEnum> list = source.getFl401().getRespondentBehaviour().getStopBehaviourTowardsApplicant();

        if (list == null) {
            return null;
        }

        List<ApplicantStopFromRespondentDoingEnum> result = new ArrayList<>();
        for (BehaviourTowardsApplicantEnum value : list) {
            result.add(ApplicantStopFromRespondentDoingEnum.getDisplayedValueFromEnumString(value.toString()));
        }
        return result;
    }

    private List<ApplicantStopFromRespondentDoingToChildEnum> mapBehaviourTowardsChildren(CourtNavFl401 source) {
        List<BehaviourTowardsChildrenEnum> list = source.getFl401().getRespondentBehaviour().getStopBehaviourTowardsChildren();

        if (list == null) {
            return null;
        }

        List<ApplicantStopFromRespondentDoingToChildEnum> result = new ArrayList<>();
        for (BehaviourTowardsChildrenEnum value : list) {
            result.add(ApplicantStopFromRespondentDoingToChildEnum.getDisplayedValueFromEnumString(value.toString()));
        }
        return result;
    }
}

