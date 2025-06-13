package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.abilitytoparticipate.AbilityToParticipate;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class AbilityToParticipateCheckerTest {

    @InjectMocks
    private AbilityToParticipateChecker abilityToParticipateChecker;

    @Mock
    private RespondentTaskErrorService respondentTaskErrorService;

    private PartyDetails respondent;

    @BeforeEach
    void setUp() {
        User user = User.builder().email("respondent@example.net")
            .idamId("1234-5678").solicitorRepresented(Yes).build();
        respondent = PartyDetails.builder()
            .user(user)
            .response(Response.builder().abilityToParticipate(
                    AbilityToParticipate.builder()
                        .factorsAffectingAbilityToParticipate(Yes)
                        .detailsOfReferralOrAssessment("Test")
                        .giveDetailsAffectingLitigationCapacity("Test")
                        .provideDetailsForFactorsAffectingAbilityToParticipate("test")
                        .build())
                          .build())
            .build();
    }

    @Test
    void isStartedTest() {
        boolean anyNonEmpty = abilityToParticipateChecker.isStarted(respondent, true);

        assertTrue(anyNonEmpty);
    }

    @Test
    void isStartedTest_scenario2() {
        PartyDetails respondent2 = PartyDetails.builder()
            .user(User.builder().email("respondent@example.net").build())
            .response(Response.builder().abilityToParticipate(
                    AbilityToParticipate.builder()
                        .factorsAffectingAbilityToParticipate(No)
                        .detailsOfReferralOrAssessment("Test")
                        .build())
                          .build())
            .build();

        boolean anyNonEmpty = abilityToParticipateChecker.isStarted(respondent2, true);

        assertTrue(anyNonEmpty);
    }


    @Test
    void isStartedNotTest() {
        boolean anyNonEmpty = abilityToParticipateChecker.isStarted(null, true);

        assertFalse(anyNonEmpty);
    }

    @Test
    void hasMandatoryCompletedTest() {
        boolean anyNonEmpty = abilityToParticipateChecker.isFinished(respondent, true);

        assertTrue(anyNonEmpty);
    }

    @Test
    void hasMandatoryCompletedWithoutRespondentTest() {
        boolean anyNonEmpty = abilityToParticipateChecker.isFinished(null, true);
        assertFalse(anyNonEmpty);
    }
}
