package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.abilitytoparticipate.AbilityToParticipate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class AbilityToParticipateCheckerTest {

    @InjectMocks
    AbilityToParticipateChecker abilityToParticipateChecker;

    @Mock
    RespondentTaskErrorService respondentTaskErrorService;

    CaseData caseData;
    PartyDetails respondent;

    PartyDetails respondent2;

    @Before
    public void setUp() {
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

        respondent2 = PartyDetails.builder()
            .user(user)
            .response(Response.builder().abilityToParticipate(
                    AbilityToParticipate.builder()
                        .factorsAffectingAbilityToParticipate(No)
                        .detailsOfReferralOrAssessment("Test")
                        .build())
                          .build())
            .build();


        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);
        doNothing().when(respondentTaskErrorService).addEventError(Mockito.any(), Mockito.any(), Mockito.any());

        caseData = CaseData.builder().respondents(respondentList).build();
    }

    @Test
    public void isStartedTest() {
        boolean anyNonEmpty = abilityToParticipateChecker.isStarted(respondent, true);

        assertTrue(anyNonEmpty);
    }

    @Test
    public void isStartedTest_scenario2() {
        boolean anyNonEmpty = abilityToParticipateChecker.isStarted(respondent2, true);

        assertTrue(anyNonEmpty);
    }


    @Test
    public void isStartedNotTest() {
        respondent = null;
        boolean anyNonEmpty = abilityToParticipateChecker.isStarted(respondent, true);

        assertFalse(anyNonEmpty);
    }

    @Test
    public void hasMandatoryCompletedTest() {
        boolean anyNonEmpty = abilityToParticipateChecker.isFinished(respondent, true);

        assertTrue(anyNonEmpty);
    }

    @Test
    public void hasMandatoryCompletedWithoutRespondentTest() {
        respondent = null;
        boolean anyNonEmpty = abilityToParticipateChecker.isFinished(respondent, true);
        assertFalse(anyNonEmpty);
    }

}
