package uk.gov.hmcts.reform.prl.services.validators;


import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.Assert.assertFalse;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

public class PdfCheckerTest {


    @Test
    void whenNoCaseDataFinishedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().build();

        PdfChecker pdfChecker = new PdfChecker();
        assertFalse(pdfChecker.isFinished(caseData));
    }

    @Test
    void whenNoCaseDataStartedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().build();

        PdfChecker pdfChecker = new PdfChecker();
        assertFalse(pdfChecker.isStarted(caseData));
    }

    @Test
    void whenNoCaseDataHasMandatoryCompletedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().build();

        PdfChecker pdfChecker = new PdfChecker();
        assertFalse(pdfChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenCaseDataPresentFinishedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name")
            .isCaseUrgent(Yes)
            .childAbductionReasons("Test string")
            .caseUrgencyTimeAndReason("Random String")
            .jurisdictionIssue(No)
            .build();

        PdfChecker pdfChecker = new PdfChecker();
        assertFalse(pdfChecker.isFinished(caseData));

    }

    @Test
    void whenCaseDataPresentStartedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name")
            .isCaseUrgent(Yes)
            .childAbductionReasons("Test string")
            .caseUrgencyTimeAndReason("Random String")
            .jurisdictionIssue(No)
            .build();

        PdfChecker pdfChecker = new PdfChecker();
        assertFalse(pdfChecker.isStarted(caseData));

    }

    @Test
    void whenCaseDataPresentHasMandatoryCompletedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name")
            .isCaseUrgent(Yes)
            .childAbductionReasons("Test string")
            .caseUrgencyTimeAndReason("Random String")
            .jurisdictionIssue(No)
            .build();


        PdfChecker pdfChecker = new PdfChecker();
        assertFalse(pdfChecker.hasMandatoryCompleted(caseData));
    }
}
