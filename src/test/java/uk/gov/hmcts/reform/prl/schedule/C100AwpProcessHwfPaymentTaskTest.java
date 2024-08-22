package uk.gov.hmcts.reform.prl.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.services.AwpProcessHwfPaymentService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(SpringExtension.class)
class C100AwpProcessHwfPaymentTaskTest {

    @InjectMocks
    C100AwpProcessHwfPaymentTask c100AwpProcessHwfPaymentTask;

    @Mock
    AwpProcessHwfPaymentService c100AwpProcessHwfPaymentService;

    @Test
    void runTaskWithHearingAwayDays() {
        c100AwpProcessHwfPaymentTask.run();

        verify(c100AwpProcessHwfPaymentService, times(1)).checkHwfPaymentStatusAndUpdateApplicationStatus();
    }
}
