package uk.gov.hmcts.reform.prl.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.services.Fm5ReminderService;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateHearingActualTask implements Runnable {

    private final Fm5ReminderService fm5ReminderService;

    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        log.info("*** FM5 reminder scheduled task is started ***");
        //Invoke fm5 reminder service to evaluate & notify if needed
        fm5ReminderService.updateHearingActuals();

        log.info("*** FM5 reminder scheduled task is completed ***");
    }
}
