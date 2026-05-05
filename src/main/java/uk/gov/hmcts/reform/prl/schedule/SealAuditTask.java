package uk.gov.hmcts.reform.prl.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.services.sealaudit.SealAuditService;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "seal-audit", name = "enabled", havingValue = "true", matchIfMissing = false)
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
