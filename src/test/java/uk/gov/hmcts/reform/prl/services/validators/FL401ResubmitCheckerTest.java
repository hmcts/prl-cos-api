package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
