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



}
