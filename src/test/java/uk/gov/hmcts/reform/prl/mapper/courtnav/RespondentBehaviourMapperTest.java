package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavRespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.Situation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsApplicantEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsChildrenEnum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RespondentBehaviourMapperTest {

    private final RespondentBehaviourMapper mapper = Mappers.getMapper(RespondentBehaviourMapper.class);

    @Test
    void shouldReturnNullWhenNonMolestationOrderNotPresent() {
        Situation situation = Situation.builder()
            .ordersAppliedFor(List.of(FL401OrderTypeEnum.occupationOrder))
            .build();

        CourtNavFl401 source = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .situation(situation)
                       .build())
            .build();

        RespondentBehaviour result = mapper.mapRespondentBehaviour(source);
        assertNull(result);
    }

    @Test
    void shouldMapBehaviourDataWhenNonMolestationOrderIsPresent() {
        CourtNavRespondentBehaviour behaviour = CourtNavRespondentBehaviour.builder()
            .stopBehaviourTowardsApplicant(List.of(BehaviourTowardsApplicantEnum.contactingApplicant))
            .stopBehaviourTowardsChildren(List.of(BehaviourTowardsChildrenEnum.harrasingOrIntimidating))
            .stopBehaviourAnythingElse("No posting on social media")
            .build();

        Situation situation = Situation.builder()
            .ordersAppliedFor(List.of(FL401OrderTypeEnum.nonMolestationOrder))
            .build();

        CourtNavFl401 source = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .respondentBehaviour(behaviour)
                       .situation(situation)
                       .build())
            .build();

        RespondentBehaviour result = mapper.mapRespondentBehaviour(source);

        assertNotNull(result);
        assertEquals(1, result.getApplicantWantToStopFromRespondentDoing().size());
        assertEquals(ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_4,
            result.getApplicantWantToStopFromRespondentDoing().getFirst());
        assertEquals(1, result.getApplicantWantToStopFromRespondentDoingToChild().size());
        assertEquals(
            ApplicantStopFromRespondentDoingToChildEnum.applicantStopFromRespondentDoingToChildEnum_Value_2,
            result.getApplicantWantToStopFromRespondentDoingToChild().getFirst());
        assertEquals("No posting on social media", result.getOtherReasonApplicantWantToStopFromRespondentDoing());
    }

    @Test
    void shouldHandleNullBehaviourLists() {
        CourtNavRespondentBehaviour behaviour = CourtNavRespondentBehaviour.builder()
            .stopBehaviourTowardsApplicant(null)
            .stopBehaviourTowardsChildren(null)
            .stopBehaviourAnythingElse("Nothing")
            .build();

        Situation situation = Situation.builder()
            .ordersAppliedFor(List.of(FL401OrderTypeEnum.nonMolestationOrder))
            .build();

        CourtNavFl401 source = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .respondentBehaviour(behaviour)
                       .situation(situation)
                       .build())
            .build();

        RespondentBehaviour result = mapper.mapRespondentBehaviour(source);

        assertNotNull(result);
        assertNull(result.getApplicantWantToStopFromRespondentDoing());
        assertNull(result.getApplicantWantToStopFromRespondentDoingToChild());
        assertEquals("Nothing", result.getOtherReasonApplicantWantToStopFromRespondentDoing());
    }
}

