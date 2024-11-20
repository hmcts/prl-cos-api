package uk.gov.hmcts.reform.prl.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.services.HwfProcessUpdateCaseStateService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(SpringExtension.class)
class HwfProcessUpdateCaseStateTaskTest {

    @InjectMocks
    HwfProcessUpdateCaseStateTask hwfProcessUpdateCaseStateTask;

    @Mock
    HwfProcessUpdateCaseStateService hwfProcessUpdateCaseStateService;

    @Test
    void runTaskWithHearingAwayDays() {
        hwfProcessUpdateCaseStateTask.run();

        verify(hwfProcessUpdateCaseStateService, times(1)).checkHwfPaymentStatusAndUpdateCaseState();
    }
}
