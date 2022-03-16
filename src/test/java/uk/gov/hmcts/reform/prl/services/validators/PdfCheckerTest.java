package uk.gov.hmcts.reform.prl.services.validators;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class PdfCheckerTest {

    @Mock
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
            .childAbductionReasons("Test string")
            .caseUrgencyTimeAndReason("Random String")
            .jurisdictionIssue(No)
            .build();

        when(pdfChecker.isFinished(caseData)).thenReturn(false);
        assertFalse(pdfChecker.isFinished(caseData));

    }

    @Test
    public void whenCaseDataPresentStartedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name")
            .isCaseUrgent(Yes)
            .childAbductionReasons("Test string")
            .caseUrgencyTimeAndReason("Random String")
            .jurisdictionIssue(No)
            .build();
        when(pdfChecker.isStarted(caseData)).thenReturn(false);
        assertFalse(pdfChecker.isStarted(caseData));
    }

    @Test
    public void whenCaseDataPresentHasMandatoryCompletedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name")
            .isCaseUrgent(Yes)
            .childAbductionReasons("Test string")
            .caseUrgencyTimeAndReason("Random String")
            .jurisdictionIssue(No)
            .build();
        when(pdfChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        assertFalse(pdfChecker.hasMandatoryCompleted(caseData));
    }
}
