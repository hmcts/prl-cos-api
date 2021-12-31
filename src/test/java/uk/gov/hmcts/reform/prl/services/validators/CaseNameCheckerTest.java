package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

public class CaseNameCheckerTest {


    @Test
    public void whenNoDataEnteredThenIsNotFinshed() {

        CaseData caseData = CaseData.builder().build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        boolean finished = caseNameChecker.isFinished(caseData);

        assert (!finished);

    }

    @Test
    public void whenCaseNameEnteredThenFinished() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name").build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        boolean finished = caseNameChecker.isFinished(caseData);

        assert (finished);

    }

    @Test
    public void whenCaseNameEnteredThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name").build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        assert !caseNameChecker.isStarted(caseData);

    }

    @Test
    public void whenNoCaseDataThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        assert !caseNameChecker.isStarted(caseData);

    }

    @Test
    public void whenCaseNameEnteredThenHasMandatoryReturnsFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name").build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        assert !caseNameChecker.hasMandatoryCompleted(caseData);

    }

    @Test
    public void whenNoCaseDataThenHasMandatoryReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        CaseNameChecker caseNameChecker = new CaseNameChecker();

        assert !caseNameChecker.hasMandatoryCompleted(caseData);

    }



}
