package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.class)
public class WelshLanguageRequirementsCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    WelshLanguageRequirementsChecker welshLanguageRequirementsChecker;

    @Test
    public void whenNoCaseDataThenIsStartedFalse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(welshLanguageRequirementsChecker.isStarted(caseData));
    }

    @Test
    public void whenNoCaseDataThenIsFinishedFalse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(welshLanguageRequirementsChecker.isFinished(caseData));
    }
}
