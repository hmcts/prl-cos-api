package uk.gov.hmcts.reform.prl.services.validators;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
public class PdfCheckerTest {

    @InjectMocks
    PdfChecker pdfChecker;

    @BeforeEach
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
