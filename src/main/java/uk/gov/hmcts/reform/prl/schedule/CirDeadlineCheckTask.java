package uk.gov.hmcts.reform.prl.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.services.CirDeadlineService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "cir", name = "cronjob.enabled")
public class CirDeadlineCheckTask implements Runnable {

    private final CirDeadlineService cirDeadlineService;

    @Override
    public void run() {
        log.info("*** CIR deadline check scheduled task started ***");
        cirDeadlineService.checkAndCreateCirOverdueTasks();
        log.info("*** CIR deadline check scheduled task completed ***");
    }
}
