package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

public class CaseNameCheckerTest {


    @Test
    public void whenNoDataEnteredThenIsNotFinshed() {

        CaseData caseData = CaseData.builder().build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        boolean finished = caseNameChecker.isFinished(caseData);

        assertFalse(finished);
    }

    @Test
    public void whenCaseNameEnteredThenFinished() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name").build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        boolean finished = caseNameChecker.isFinished(caseData);

        assertTrue(finished);
    }

    @Test
    public void whenCaseNameEnteredThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name").build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        assertFalse(caseNameChecker.isStarted(caseData));
    }

    @Test
    public void whenNoCaseDataThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        assertFalse(caseNameChecker.isStarted(caseData));
    }

    @Test
    public void whenCaseNameEnteredThenHasMandatoryReturnsFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name").build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        assertFalse(caseNameChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenNoCaseDataThenHasMandatoryReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        assertFalse(caseNameChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        CaseNameChecker caseNameChecker = new CaseNameChecker();
        assertNotNull(caseNameChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
