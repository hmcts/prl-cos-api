package uk.gov.hmcts.reform.prl.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.services.UpdateHearingActualsService;
import uk.gov.hmcts.reform.prl.services.requestorder.RequestOrderTaskService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class UpdateHearingActualTaskTest {

    @InjectMocks
    UpdateHearingActualTask updateHearingActualTask;

    @Mock
    UpdateHearingActualsService updateHearingActualsService;

    @Mock
    RequestOrderTaskService requestOrderTaskService;

    @Test
    void runInvokesBothServicesInSequence() {
        updateHearingActualTask.run();

        verify(updateHearingActualsService, times(1)).updateHearingActuals();
        verify(requestOrderTaskService, times(1)).processRequestOrderTasks();
    }
}
