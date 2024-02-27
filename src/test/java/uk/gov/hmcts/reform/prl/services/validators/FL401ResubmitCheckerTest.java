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
public class FL401ResubmitCheckerTest {

    @Mock
    FL401StatementOfTruthAndSubmitChecker fl401StatementOfTruthAndSubmitChecker;

    @InjectMocks
    FL401ResubmitChecker fl401ResubmitChecker;

    @Test
    public void whenSubmitAndPayCheckerFinished_thenSubmitCheckerAlsoFinished() {
        CaseData caseData = CaseData.builder().build();
        when(fl401StatementOfTruthAndSubmitChecker.isFinished(caseData)).thenReturn(true);
        assertTrue(fl401ResubmitChecker.isFinished(caseData));

    }

    @Test
    public void whenNoCaseData_thenSubmitCheckerNotStarted() {
        CaseData caseData = CaseData.builder().build();
        assertFalse(fl401ResubmitChecker.isStarted(caseData));
    }

    @Test
    public void whenNoCaseData_thenSubmitCheckerHasMandatoryCompletedFalse() {
        CaseData caseData = CaseData.builder().build();
        assertFalse(fl401ResubmitChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(fl401ResubmitChecker.getDefaultTaskState(CaseData.builder().build()));
    }

}
