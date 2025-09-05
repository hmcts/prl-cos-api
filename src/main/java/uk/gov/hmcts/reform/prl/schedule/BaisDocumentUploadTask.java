package uk.gov.hmcts.reform.prl.schedule;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.services.acro.BaisDocumentUploadService;

@Component
@Slf4j
@RequiredArgsConstructor
public class BaisDocumentUploadTask implements Runnable {

    private final BaisDocumentUploadService baisDocumentUploadService;

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
        log.info("*** Uploading FL404A order to Bais ***");
        //Invoke Bais document upload service to upload FL404a orders
        baisDocumentUploadService.uploadFL404Orders();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("*** Uploading FL404A order to Bais task is completed ***");
    }
}
