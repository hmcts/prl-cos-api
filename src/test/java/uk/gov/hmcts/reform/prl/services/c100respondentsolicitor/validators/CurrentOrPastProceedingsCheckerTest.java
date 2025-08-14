package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
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
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentProceedingDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class CurrentOrPastProceedingsCheckerTest {

    @InjectMocks
    CurrentOrPastProceedingsChecker currentOrPastProceedingsChecker;

    @Mock
    RespondentTaskErrorService respondentTaskErrorService;

    CaseData caseData;

    PartyDetails respondent;

    @Before
    public void setUp() {

        RespondentProceedingDetails proceedingDetails = RespondentProceedingDetails.builder()
            .caseNumber("122344")
            .nameAndOffice("testoffice")
            .nameOfCourt("testCourt")
            .build();

        Element<RespondentProceedingDetails> proceedingDetailsElement = Element.<RespondentProceedingDetails>builder()
            .value(proceedingDetails).build();
        List<Element<RespondentProceedingDetails>> proceedingsList = Collections.singletonList(proceedingDetailsElement);


        User user = User.builder().email("respondent@example.net")
            .idamId("1234-5678").solicitorRepresented(Yes).build();

        respondent = PartyDetails.builder()
            .user(user)
            .response(Response
                          .builder()
                          .currentOrPastProceedingsForChildren(YesNoDontKnow.yes)
                          .respondentExistingProceedings(proceedingsList)
                          .build())
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);
        doNothing().when(respondentTaskErrorService).addEventError(Mockito.any(), Mockito.any(), Mockito.any());

        caseData = CaseData.builder().respondents(respondentList).build();
    }

    @Test
    public void isStartedTest() {
        boolean anyNonEmpty = currentOrPastProceedingsChecker.isStarted(respondent, true);

        assertTrue(anyNonEmpty);
    }

    @Test
    public void isStartedNotTest() {
        respondent = null;
        boolean anyNonEmpty = currentOrPastProceedingsChecker.isStarted(respondent, true);

        assertFalse(anyNonEmpty);
    }

    @Test
    public void hasMandatoryCompletedTest() {
        boolean anyNonEmpty = currentOrPastProceedingsChecker.isFinished(respondent, true);
        Assert.assertTrue(anyNonEmpty);
    }

    @Test
    public void hasMandatoryCompletedWithoutRespdntExisProceedingAndPastProceedingTest() {
        respondent = PartyDetails.builder()
            .response(Response
                          .builder()
                          .build())
            .build();
        boolean anyNonEmpty = currentOrPastProceedingsChecker.isFinished(respondent, true);
        Assert.assertFalse(anyNonEmpty);
    }

    @Test
    public void hasMandatoryCompletedPastProceedingAsNoTest() {
        respondent = PartyDetails.builder()
            .response(Response
                          .builder()
                          .currentOrPastProceedingsForChildren(YesNoDontKnow.no)
                          .build())
            .build();
        boolean anyNonEmpty = currentOrPastProceedingsChecker.isFinished(respondent, true);
        Assert.assertTrue(anyNonEmpty);
    }

    @Test
    public void hasMandatoryNotCompletedPastProceedingAsEmptyValeuTest() {
        respondent = PartyDetails.builder()
            .response(Response
                          .builder()
                          .currentOrPastProceedingsForChildren(YesNoDontKnow.no)
                          .build())
            .build();
        boolean anyNonEmpty = currentOrPastProceedingsChecker.isFinished(respondent, true);
        Assert.assertTrue(anyNonEmpty);
    }

}
