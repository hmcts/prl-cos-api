package uk.gov.hmcts.reform.prl.schedule;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.services.HwfProcessUpdateCaseStateService;

@Component
@Slf4j
@RequiredArgsConstructor
public class HwfProcessUpdateCaseStateTask implements Runnable {

    private final HwfProcessUpdateCaseStateService hwfProcessUpdateCaseStateService;

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
        log.info("*** Hwf process update case state scheduled task is started ***");
        //Invoke hwfProcessUpdateCaseStateService to get service request status and update case state for successful payment
        hwfProcessUpdateCaseStateService.checkHwfPaymentStatusAndUpdateCaseState();

        log.info("*** Hwf process update case state scheduled task is completed ***");
    }
}
