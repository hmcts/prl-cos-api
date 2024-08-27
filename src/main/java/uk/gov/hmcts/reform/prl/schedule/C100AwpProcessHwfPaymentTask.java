package uk.gov.hmcts.reform.prl.schedule;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.services.AwpProcessHwfPaymentService;

@Component
@Slf4j
@RequiredArgsConstructor
public class C100AwpProcessHwfPaymentTask implements Runnable {

    private final AwpProcessHwfPaymentService awpProcessHwfPaymentService;

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
        log.info("*** Process Citizen C100 additional applications with HWF task is started ***");
        //Invoke C100 Awp process Hwf payment service to evaluate & update application status
        awpProcessHwfPaymentService.checkHwfPaymentStatusAndUpdateApplicationStatus();

        log.info("*** Process Citizen C100 additional applications with HWF task is completed ***");
    }
}
