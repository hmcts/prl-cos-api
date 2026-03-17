package uk.gov.hmcts.reform.prl.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.services.CirDeadlineService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = "cir.cronjob.enabled=true")
class CirDeadlineCheckTaskTest {

    @InjectMocks
    CirDeadlineCheckTask cirDeadlineCheckTask;

    @Mock
    CirDeadlineService cirDeadlineService;

    @Test
    void runTaskInvokesCirDeadlineService() {
        cirDeadlineCheckTask.run();

        verify(cirDeadlineService, times(1)).checkAndCreateCirOverdueTasks();
    }
}
