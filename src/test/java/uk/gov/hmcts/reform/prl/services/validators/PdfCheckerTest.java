package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;


import static uk.gov.hmcts.reform.prl.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;

public class PdfCheckerTest {


    @Test
    public void whenNoCaseDataFinishedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().build();

        PDFChecker pdfChecker = new PDFChecker();
        assert !pdfChecker.isFinished(caseData);

    }

    @Test
    public void whenNoCaseDataStartedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().build();

        PDFChecker pdfChecker = new PDFChecker();
        assert !pdfChecker.isStarted(caseData);

    }

    @Test
    public void whenNoCaseDataHasMandatoryCompletedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().build();

        PDFChecker pdfChecker = new PDFChecker();
        assert !pdfChecker.hasMandatoryCompleted(caseData);

    }

    @Test
    public void whenCaseDataPresentFinishedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name")
            .isCaseUrgent(YES)
            .childAbductionReasons("Test string")
            .caseUrgencyTimeAndReason("Random String")
            .jurisdictionIssue(NO)
            .build();

        PDFChecker pdfChecker = new PDFChecker();
        assert !pdfChecker.isFinished(caseData);

    }

    @Test
    public void whenCaseDataPresentStartedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name")
            .isCaseUrgent(YES)
            .childAbductionReasons("Test string")
            .caseUrgencyTimeAndReason("Random String")
            .jurisdictionIssue(NO)
            .build();

        PDFChecker pdfChecker = new PDFChecker();
        assert !pdfChecker.isStarted(caseData);

    }

    @Test
    public void whenCaseDataPresentHasMandatoryCompletedShouldReturnFalse() {
        CaseData caseData = CaseData.builder().applicantCaseName("Test Name")
            .isCaseUrgent(YES)
            .childAbductionReasons("Test string")
            .caseUrgencyTimeAndReason("Random String")
            .jurisdictionIssue(NO)
            .build();


        PDFChecker pdfChecker = new PDFChecker();
        assert !pdfChecker.hasMandatoryCompleted(caseData);

    }

}
