package uk.gov.hmcts.reform.prl.services.validators;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.Assert.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class PdfCheckerTest {

    @InjectMocks
    PdfChecker pdfChecker;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void whenNoCaseDataFinishedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().build();
        assertFalse(pdfChecker.isFinished(caseData));
    }

    @Test
    public void whenNoCaseDataStartedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().build();
        assertFalse(pdfChecker.isStarted(caseData));
    }

    @Test
    public void whenNoCaseDataHasMandatoryCompletedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().build();
        assertFalse(pdfChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenCaseDataPresentFinishedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name")
            .isCaseUrgent(Yes)
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .childAbductionReasons("Test string").build())
            .caseUrgencyTimeAndReason("Random String")
            .jurisdictionIssue(No)
            .build();

        assertFalse(pdfChecker.isFinished(caseData));

    }

    @Test
    public void whenCaseDataPresentStartedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name")
            .isCaseUrgent(Yes)
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .childAbductionReasons("Test string").build())
            .caseUrgencyTimeAndReason("Random String")
            .jurisdictionIssue(No)
            .build();

        assertFalse(pdfChecker.isStarted(caseData));
    }

    @Test
    public void whenCaseDataPresentHasMandatoryCompletedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name")
            .isCaseUrgent(Yes)
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .childAbductionReasons("Test string").build())
            .caseUrgencyTimeAndReason("Random String")
            .jurisdictionIssue(No)
            .build();

        assertFalse(pdfChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(pdfChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
