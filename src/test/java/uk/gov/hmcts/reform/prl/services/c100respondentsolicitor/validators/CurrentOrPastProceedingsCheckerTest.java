package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentProceedingDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CurrentOrPastProceedingsCheckerTest {

    @InjectMocks
    CurrentOrPastProceedingsChecker currentOrPastProceedingsChecker;

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

        caseData = CaseData.builder().respondents(respondentList).build();
    }

    @Test
    public void isStartedTest() {
        boolean anyNonEmpty = currentOrPastProceedingsChecker.isStarted(respondent);

        assertTrue(anyNonEmpty);
    }

    @Test
    public void hasMandatoryCompletedTest() {
        boolean anyNonEmpty = currentOrPastProceedingsChecker.isFinished(respondent);
        Assert.assertTrue(anyNonEmpty);
    }
}
