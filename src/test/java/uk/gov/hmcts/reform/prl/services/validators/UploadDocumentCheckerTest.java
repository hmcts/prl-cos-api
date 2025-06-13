package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class UploadDocumentCheckerTest {

    @Mock
    TaskErrorService taskErrorService;


    @InjectMocks
    UploadDocumentChecker uploadDocumentChecker;

    @Test
    void whenNoCaseDataPresentThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(uploadDocumentChecker.isStarted(caseData));
    }

    @Test
    void whenAllChildDataPresentThenIsFinishedReturnsFalseWithNullYes() {

        CaseData caseData = CaseData.builder()
            .childrenNotInTheCase(null)
            .childrenNotPartInTheCaseYesNo(null)
            .build();
        assertFalse(uploadDocumentChecker.isFinished(caseData));
    }


    @Test
    void whenNoCaseDataPresentThenHasMandatoryCompletedReturnsTrue() {
        CaseData caseData = CaseData.builder().build();
        assertFalse(uploadDocumentChecker.hasMandatoryCompleted(caseData));
    }


    @Test
    void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(uploadDocumentChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
