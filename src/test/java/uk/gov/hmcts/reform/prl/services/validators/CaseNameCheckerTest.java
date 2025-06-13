package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CaseNameCheckerTest {


    @Test
    void whenNoDataEnteredThenIsNotFinshed() {

        CaseData caseData = CaseData.builder().build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        boolean finished = caseNameChecker.isFinished(caseData);

        assertFalse(finished);
    }

    @Test
    void whenCaseNameEnteredThenFinished() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name").build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        boolean finished = caseNameChecker.isFinished(caseData);

        assertTrue(finished);
    }

    @Test
    void whenCaseNameEnteredThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name").build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        assertFalse(caseNameChecker.isStarted(caseData));
    }

    @Test
    void whenNoCaseDataThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        assertFalse(caseNameChecker.isStarted(caseData));
    }

    @Test
    void whenCaseNameEnteredThenHasMandatoryReturnsFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name").build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        assertFalse(caseNameChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenNoCaseDataThenHasMandatoryReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        assertFalse(caseNameChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        CaseNameChecker caseNameChecker = new CaseNameChecker();
        assertNotNull(caseNameChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
