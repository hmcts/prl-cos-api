package uk.gov.hmcts.reform.prl.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.services.UpdateHearingActualsService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class UpdateHearingActualTaskTest {

    @InjectMocks
    UpdateHearingActualTask updateHearingActualTask;

    @Mock
    UpdateHearingActualsService updateHearingActualsService;

    @Test
    void runTaskWithHearingAwayDays() {
        updateHearingActualTask.run();

        verify(updateHearingActualsService, times(1)).updateHearingActuals();
    }
}
