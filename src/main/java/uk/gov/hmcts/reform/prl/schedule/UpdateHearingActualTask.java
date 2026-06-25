package uk.gov.hmcts.reform.prl.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.services.UpdateHearingActualsService;
import uk.gov.hmcts.reform.prl.services.requestorder.RequestOrderTaskService;

/**
 * TODO: this class is now misnamed — it does both the original today-only hearing-actuals
 * fire AND the new per-hearing Request Order chase. {@code InFlightHearingTaskGenerator} or
 * similar would describe it better. Rename in lockstep with cnp-flux-config (prod) and
 * the prl-ccd-definitions preview values when there's appetite for the cross-repo change.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateHearingActualTask implements Runnable {

    private final UpdateHearingActualsService updateHearingActualsService;
    private final RequestOrderTaskService requestOrderTaskService;

    @Override
    public void run() {
        //Invoke fm5 reminder service to evaluate & notify if needed
        updateHearingActualsService.updateHearingActuals();
        requestOrderTaskService.processRequestOrderTasks();
    }
}
