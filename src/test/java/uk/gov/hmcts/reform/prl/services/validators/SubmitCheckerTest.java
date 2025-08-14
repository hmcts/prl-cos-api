package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class SubmitCheckerTest {

    @Mock
    SubmitAndPayChecker submitAndPayChecker;

    @InjectMocks
    SubmitChecker submitChecker;

    @Test
    public void whenSubmitAndPayCheckerFinished_thenSubmitCheckerAlsoFinished() {
        CaseData caseData = CaseData.builder().build();
        when(submitAndPayChecker.isFinished(caseData)).thenReturn(true);
        assertTrue(submitChecker.isFinished(caseData));

    }

    @Test
    public void whenNoCaseData_thenSubmitCheckerNotStarted() {
        CaseData caseData = CaseData.builder().build();
        assertFalse(submitChecker.isStarted(caseData));
    }

    @Test
    public void whenNoCaseData_thenSubmitCheckerHasMandatoryCompletedFalse() {
        CaseData caseData = CaseData.builder().build();
        assertFalse(submitChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(submitChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
