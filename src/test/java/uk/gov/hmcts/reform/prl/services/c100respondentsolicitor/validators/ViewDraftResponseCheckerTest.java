package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ViewDraftResponseCheckerTest {

    @InjectMocks
    ViewDraftResponseChecker viewDraftResponseChecker;

    PartyDetails respondent;

    @Before
    public void setUp() {
        respondent = PartyDetails.builder().build();
    }

    @Test
    public void isStartedTest() {

        boolean bool = viewDraftResponseChecker.isStarted(respondent, true);

        assertFalse(bool);
    }

    @Test
    public void mandatoryCompletedTest() {
        boolean bool = viewDraftResponseChecker.isFinished(respondent, true);

        assertFalse(bool);
    }
}
