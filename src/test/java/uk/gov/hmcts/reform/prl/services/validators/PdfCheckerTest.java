package uk.gov.hmcts.reform.prl.services.validators;


import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

public class PdfCheckerTest {


    @Test
    public void whenNoCaseDataFinishedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().build();

        PdfChecker pdfChecker = new PdfChecker();
        assert !pdfChecker.isFinished(caseData);

    }

    @Test
    public void whenNoCaseDataStartedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().build();

        PdfChecker pdfChecker = new PdfChecker();
        assert !pdfChecker.isStarted(caseData);

    }

    @Test
    public void whenNoCaseDataHasMandatoryCompletedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().build();

        PdfChecker pdfChecker = new PdfChecker();
        assert !pdfChecker.hasMandatoryCompleted(caseData);

    }

    @Test
    public void whenCaseDataPresentFinishedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name")
            .isCaseUrgent(Yes)
            .childAbductionReasons("Test string")
            .caseUrgencyTimeAndReason("Random String")
            .jurisdictionIssue(No)
            .build();

        PdfChecker pdfChecker = new PdfChecker();
        assert !pdfChecker.isFinished(caseData);

    }

    @Test
    public void whenCaseDataPresentStartedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name")
            .isCaseUrgent(Yes)
            .childAbductionReasons("Test string")
            .caseUrgencyTimeAndReason("Random String")
            .jurisdictionIssue(No)
            .build();

        PdfChecker pdfChecker = new PdfChecker();
        assert !pdfChecker.isStarted(caseData);

    }

    @Test
    public void whenCaseDataPresentHasMandatoryCompletedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name")
            .isCaseUrgent(Yes)
            .childAbductionReasons("Test string")
            .caseUrgencyTimeAndReason("Random String")
            .jurisdictionIssue(No)
            .build();


        PdfChecker pdfChecker = new PdfChecker();
        assert !pdfChecker.hasMandatoryCompleted(caseData);

    }

}
