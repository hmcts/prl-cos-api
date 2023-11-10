package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.PermissionRequiredEnum.noNotRequired;
import static uk.gov.hmcts.reform.prl.enums.PermissionRequiredEnum.noNowSought;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationTypeCheckerTest {

    @Mock
    private TaskErrorService taskErrorService;

    @InjectMocks
    private ApplicationTypeChecker applicationTypeChecker;

    @Test
    public void whenFieldsPartiallyCompleteIsFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder()
            .ordersApplyingFor(Collections.singletonList(childArrangementsOrder))
            .applicationDetails("Test details")
            .build();

        assertTrue(!applicationTypeChecker.isFinished(caseData));
    }

    @Test
    public void whenAllRequiredFieldsCompletedThenIsFinishedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .ordersApplyingFor(Collections.singletonList(childArrangementsOrder))
            .natureOfOrder("Test")
            .consentOrder(Yes)
            .applicationPermissionRequired(noNotRequired)
            .applicationDetails("Test details")
            .build();

        assertTrue(applicationTypeChecker.isFinished(caseData));
    }

    @Test
    public void whenTypeOfApplicationNoNowSoughtSelectedThenIsFinishedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .ordersApplyingFor(Collections.singletonList(childArrangementsOrder))
            .natureOfOrder("Test")
            .consentOrder(Yes)
            .applicationPermissionRequired(noNowSought)
            .applicationDetails("Test details")
            .build();

        assertTrue(applicationTypeChecker.isFinished(caseData));
    }

    @Test
    public void whenAnyFieldCompletedThenIsStartedReturnsTrue() {

        CaseData caseData = CaseData.builder()
            .natureOfOrder("Test")
            .build();

        assertTrue(applicationTypeChecker.isStarted(caseData));
    }

    @Test
    public void whenNoCaseDataThenIsStartedReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(applicationTypeChecker.isStarted(caseData));

    }

    @Test
    public void whenNoCaseDataThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(applicationTypeChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenCaseDataPresentThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder()
            .ordersApplyingFor(Collections.singletonList(childArrangementsOrder))
            .natureOfOrder("Test")
            .consentOrder(Yes)
            .applicationPermissionRequired(noNotRequired)
            .applicationDetails("Test details")
            .build();

        assertFalse(applicationTypeChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(applicationTypeChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
