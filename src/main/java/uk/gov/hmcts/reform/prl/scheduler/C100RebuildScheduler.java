package uk.gov.hmcts.reform.prl.scheduler;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class C100RebuildScheduler {

    @Scheduled(cron = "10 * * * * *")
    @SchedulerLock(name = "scheduledTaskName")
    public void emailNotification() throws InterruptedException {
        log.info("Email scheduler running");
    }
}
