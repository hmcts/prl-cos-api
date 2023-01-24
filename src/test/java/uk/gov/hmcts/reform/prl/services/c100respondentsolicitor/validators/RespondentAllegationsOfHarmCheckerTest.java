package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RespondentAllegationsOfHarmCheckerTest {

    @InjectMocks
    RespondentAllegationsOfHarmChecker respondentAllegationsOfHarmChecker;

    CaseData caseData;

    @Before
    public void setUp(){

        PartyDetails respondent = PartyDetails.builder().build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        caseData = CaseData.builder().respondents(respondentList).build();
    }
}
