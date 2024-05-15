package uk.gov.hmcts.reform.prl.schedule;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.services.Fm5ReminderService;

@Component
@Slf4j
@RequiredArgsConstructor
public class Fm5ReminderNotificationTask implements Runnable {

    private final Fm5ReminderService fm5ReminderService;
    public static final String HEARING_AWAY_DAYS = "HEARING_AWAY_DAYS";

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     *
     * <p>The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        log.info("*** FM5 reminder scheduled task is started ***");
        log.info("HearingAwayDays from the environment variable {}", System.getenv(HEARING_AWAY_DAYS));
        //Invoke fm5 reminder service to evaluate & notify if needed
        fm5ReminderService.sendFm5ReminderNotifications(null != System.getenv(HEARING_AWAY_DAYS)
                                                            ? Long.parseLong(System.getenv(HEARING_AWAY_DAYS))
                                                            : 18L);

        log.info("*** FM5 reminder scheduled task is completed ***");
    }
}
