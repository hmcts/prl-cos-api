package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class UploadDocumentCheckerTest {

    @Mock
    TaskErrorService taskErrorService;


    @InjectMocks
    UploadDocumentChecker uploadDocumentChecker;

    @Test
    public void whenNoCaseDataPresentThenIsStartedReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assertTrue(!uploadDocumentChecker.isStarted(caseData));
    }

    @Test
    public void whenAllChildDataPresentThenIsFinishedReturnsFalseWithNullYes() {

        CaseData caseData = CaseData.builder()
            .childrenNotInTheCase(null)
            .childrenNotPartInTheCaseYesNo(null)
            .build();
        assertTrue(!uploadDocumentChecker.isFinished(caseData));
    }


    @Test
    public void whenNoCaseDataPresentThenHasMandatoryCompletedReturnsTrue() {
        CaseData caseData = CaseData.builder().build();
        assertTrue(!uploadDocumentChecker.hasMandatoryCompleted(caseData));
    }


    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(uploadDocumentChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
