package uk.gov.hmcts.reform.prl.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.services.requestorder.RequestOrderTaskService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class RequestOrderTaskTest {

    @InjectMocks
    RequestOrderTask requestOrderTask;


    @Mock
    RequestOrderTaskService requestOrderTaskService;

    @Test
    void runInvokesBothServicesInSequence() {
        requestOrderTask.run();

        verify(requestOrderTaskService, times(1)).processRequestOrderTasks();
    }
}
