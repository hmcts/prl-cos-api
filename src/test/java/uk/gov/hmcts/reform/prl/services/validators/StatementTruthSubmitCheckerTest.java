package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class StatementTruthSubmitCheckerTest {

    @Mock
    TaskErrorService taskErrorService;


    @InjectMocks
    StatementTruthSubmitChecker statementTruthSubmitChecker;

    @Test
    public void whenNoCaseDataPresentThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assertTrue(!statementTruthSubmitChecker.isStarted(caseData));
    }

    @Test
    public void whenAllChildDataPresentThenIsFinishedReturnsFalseWithNullYes() {

        CaseData caseData = CaseData.builder()
            .childrenNotInTheCase(null)
            .childrenNotPartInTheCaseYesNo(null)
            .build();
        assertTrue(!statementTruthSubmitChecker.isFinished(caseData));
    }


    @Test
    public void whenNoCaseDataPresentThenHasMandatoryCompletedReturnsTrue() {
        CaseData caseData = CaseData.builder().build();
        assertTrue(!statementTruthSubmitChecker.hasMandatoryCompleted(caseData));
    }


    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(statementTruthSubmitChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
