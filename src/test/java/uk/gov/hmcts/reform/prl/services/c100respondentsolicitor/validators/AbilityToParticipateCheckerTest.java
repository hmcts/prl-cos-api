package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorAbilityToParticipateInProceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AbilityToParticipateCheckerTest {

    @InjectMocks
    AbilityToParticipateChecker abilityToParticipateChecker;

    @Mock
    RespondentTaskErrorService respondentTaskErrorService;

    CaseData caseData;
    PartyDetails respondent;

    @Before
    public void setUp() {
        User user = User.builder().email("respondent@example.net")
            .idamId("1234-5678").solicitorRepresented(Yes).build();

        respondent = PartyDetails.builder()
            .user(user)
            .response(Response.builder().abilityToParticipate(
                SolicitorAbilityToParticipateInProceedings.builder()
                    .factorsAffectingAbilityToParticipate(YesNoDontKnow.yes)
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
        boolean anyNonEmpty = abilityToParticipateChecker.isStarted(respondent);

        assertTrue(anyNonEmpty);
    }

    @Test
    public void hasMandatoryCompletedTest() {
        boolean anyNonEmpty = abilityToParticipateChecker.isFinished(respondent);

        assertFalse(anyNonEmpty);
    }
}
