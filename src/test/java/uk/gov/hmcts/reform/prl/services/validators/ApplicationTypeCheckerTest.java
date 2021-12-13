package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;

import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.PermissionRequiredEnum.noNotRequired;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;

public class ApplicationTypeCheckerTest {

    @Test
    public void whenFieldsPartiallyCompleteIsFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder()
            .ordersApplyingFor(Collections.singletonList(childArrangementsOrder))
            .applicationDetails("Test details")
            .build();

        ApplicationTypeChecker applicationTypeChecker = new ApplicationTypeChecker();

        assert !applicationTypeChecker.isFinished(caseData);

    }

    @Test
    public void whenAllRequiredFieldsCompletedThenIsFinishedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .ordersApplyingFor(Collections.singletonList(childArrangementsOrder))
            .natureOfOrder("Test")
            .consentOrder(YES)
            .applicationPermissionRequired(noNotRequired)
            .applicationDetails("Test details")
            .build();

        ApplicationTypeChecker applicationTypeChecker = new ApplicationTypeChecker();

        assert applicationTypeChecker.isFinished(caseData);

    }

    @Test
    public void whenAnyFieldCompletedThenIsStartedReturnsTrue() {

        CaseData caseData = CaseData.builder()
            .natureOfOrder("Test")
            .build();

        ApplicationTypeChecker applicationTypeChecker = new ApplicationTypeChecker();

        assert applicationTypeChecker.isStarted(caseData);

    }

    @Test
    public void whenNoCaseDataThenIsStartedReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        ApplicationTypeChecker applicationTypeChecker = new ApplicationTypeChecker();

        assert !applicationTypeChecker.isStarted(caseData);

    }

    @Test
    public void whenNoCaseDataThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        ApplicationTypeChecker applicationTypeChecker = new ApplicationTypeChecker();

        assert !applicationTypeChecker.hasMandatoryCompleted(caseData);

    }

    @Test
    public void whenCaseDataPresentThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder()
            .ordersApplyingFor(Collections.singletonList(childArrangementsOrder))
            .natureOfOrder("Test")
            .consentOrder(YES)
            .applicationPermissionRequired(noNotRequired)
            .applicationDetails("Test details")
            .build();

        ApplicationTypeChecker applicationTypeChecker = new ApplicationTypeChecker();

        assert !applicationTypeChecker.hasMandatoryCompleted(caseData);

    }


}
