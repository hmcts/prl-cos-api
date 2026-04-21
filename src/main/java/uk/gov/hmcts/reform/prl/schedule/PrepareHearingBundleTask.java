package uk.gov.hmcts.reform.prl.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.services.PrepareHearingBundleService;

@Component
@Slf4j
@RequiredArgsConstructor
public class PrepareHearingBundleTask implements Runnable {

    private final PrepareHearingBundleService prepareHearingBundleService;

    @Override
    public void run() {
        // Find all cases with hearings in 5 days, create a WA task on them
        prepareHearingBundleService.searchForHearingsIn5DaysAndCreateTasks();
    }
}
