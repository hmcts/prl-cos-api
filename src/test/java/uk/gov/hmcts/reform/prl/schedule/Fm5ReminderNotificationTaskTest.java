package uk.gov.hmcts.reform.prl.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.services.Fm5ReminderService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(SpringExtension.class)
class Fm5ReminderNotificationTaskTest {

    @InjectMocks
    Fm5ReminderNotificationTask fm5ReminderNotificationTask;

    @Mock
    Fm5ReminderService fm5ReminderService;

    @Test
    void runTaskWithHearingAwayDays() {
        fm5ReminderNotificationTask.run();

        verify(fm5ReminderService, times(1)).sendFm5ReminderNotifications(18L);
    }
}
