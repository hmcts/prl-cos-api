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
class AwpProcessHwfPaymentTaskTest {

    @InjectMocks
    AwpProcessHwfPaymentTask awpProcessHwfPaymentTask;

    @Mock
    AwpProcessHwfPaymentService awpProcessHwfPaymentService;

    @Test
    void runTaskWithHearingAwayDays() {
        awpProcessHwfPaymentTask.run();

        verify(awpProcessHwfPaymentService, times(1)).checkHwfPaymentStatusAndUpdateApplicationStatus();
    }
}
