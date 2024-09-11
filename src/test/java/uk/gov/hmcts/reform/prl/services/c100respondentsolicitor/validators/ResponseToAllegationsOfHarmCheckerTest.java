package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResponseToAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class ResponseToAllegationsOfHarmCheckerTest {

    @InjectMocks
    ResponseToAllegationsOfHarmChecker responseToAllegationsOfHarmChecker;

    @Mock
    RespondentTaskErrorService respondentTaskErrorService;

    CaseData caseData;
    PartyDetails respondent1;

    PartyDetails respondent2;

    @Before
    public void setUp() {
        User user = User.builder().email("respondent@example.net")
                .idamId("1234-5678").solicitorRepresented(Yes).build();
        respondent1 = PartyDetails.builder()
                .user(user)
                .response(Response.builder()
                        .responseToAllegationsOfHarm(
                                ResponseToAllegationsOfHarm.builder()
                                        .responseToAllegationsOfHarmYesOrNoResponse(Yes)
                                        .responseToAllegationsOfHarmDocument(Document.builder().build())
                                        .build())
                        .build())
                .build();

        respondent2 = PartyDetails.builder()
                .user(user)
                .response(Response.builder()
                        .responseToAllegationsOfHarm(
                                ResponseToAllegationsOfHarm.builder()
                                        .responseToAllegationsOfHarmYesOrNoResponse(No)
                                        .build())
                        .build())
                .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent1).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        caseData = CaseData.builder().respondents(respondentList).build();
    }

    @Test
    public void isStartedTest() {
        boolean anyNonEmpty = responseToAllegationsOfHarmChecker.isStarted(respondent1, true);

        assertTrue(anyNonEmpty);
    }

    @Test
    public void isStartedTest_scenario2() {
        boolean anyNonEmpty = responseToAllegationsOfHarmChecker.isStarted(respondent2, true);

        assertTrue(anyNonEmpty);
    }

    @Test
    public void isStartedTest_scenario3() {
        boolean anyNonEmpty = responseToAllegationsOfHarmChecker.isStarted(respondent1, false);

        assertTrue(anyNonEmpty);
    }

    @Test
    public void isStartedNotTest() {
        respondent1 = null;
        boolean anyNonEmpty = responseToAllegationsOfHarmChecker.isStarted(respondent1, true);

        assertFalse(anyNonEmpty);
    }

    @Test
    public void hasMandatoryCompletedTest() {
        boolean anyNonEmpty = responseToAllegationsOfHarmChecker.isFinished(respondent1, true);

        assertTrue(anyNonEmpty);
    }

    @Test
    public void hasMandatoryCompletedWithoutRespondentTest() {
        respondent1 = null;
        boolean anyNonEmpty = responseToAllegationsOfHarmChecker.isFinished(respondent1, true);
        assertFalse(anyNonEmpty);
    }

    @Test
    public void hasMandatoryCompletedTestWithoutC1A() {
        boolean anyNonEmpty = responseToAllegationsOfHarmChecker.isFinished(respondent1, false);

        assertTrue(anyNonEmpty);
    }

}
