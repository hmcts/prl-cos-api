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
class WelshLanguageRequirementsCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    WelshLanguageRequirementsChecker welshLanguageRequirementsChecker;

    @Test
    void whenNoCaseDataThenIsStartedFalse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(welshLanguageRequirementsChecker.isStarted(caseData));
    }

    @Test
    void whenNoCaseDataThenIsFinishedFalse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(welshLanguageRequirementsChecker.isFinished(caseData));
    }

    @Test
    void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(welshLanguageRequirementsChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
