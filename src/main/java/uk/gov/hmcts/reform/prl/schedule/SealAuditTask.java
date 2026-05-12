package uk.gov.hmcts.reform.prl.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.services.sealaudit.SealAuditService;

@Component
@Slf4j
@RequiredArgsConstructor
public class SealAuditTask implements Runnable {

    private final SealAuditService sealAuditService;

    @Override
    public void run() {
        log.info("*** Starting Seal Audit Task ***");
        try {
            sealAuditService.runAudit();
        } catch (Exception e) {
            log.error("Seal Audit Task failed", e);
            throw e;
        }
        log.info("*** Seal Audit Task completed ***");
    }
}
