package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.PermissionRequiredEnum.noNotRequired;
import static uk.gov.hmcts.reform.prl.enums.PermissionRequiredEnum.noNowSought;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class ApplicationTypeCheckerTest {

    @Mock
    private TaskErrorService taskErrorService;

    @InjectMocks
    private ApplicationTypeChecker applicationTypeChecker;

    @Test
    void whenFieldsPartiallyCompleteIsFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder()
            .ordersApplyingFor(Collections.singletonList(childArrangementsOrder))
            .applicationDetails("Test details")
            .build();

        assertFalse(applicationTypeChecker.isFinished(caseData));
    }

    @Test
    void whenAllRequiredFieldsCompletedThenIsFinishedReturnsTrue() {
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
    void whenTypeOfApplicationNoNowSoughtSelectedThenIsFinishedReturnsTrue() {
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
    void whenAnyFieldCompletedThenIsStartedReturnsTrue() {

        CaseData caseData = CaseData.builder()
            .natureOfOrder("Test")
            .build();

        assertTrue(applicationTypeChecker.isStarted(caseData));
    }

    @Test
    void whenNoCaseDataThenIsStartedReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(applicationTypeChecker.isStarted(caseData));

    }

    @Test
    void whenNoCaseDataThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(applicationTypeChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenCaseDataPresentThenHasMandatoryReturnsFalse() {

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
    void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(applicationTypeChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
