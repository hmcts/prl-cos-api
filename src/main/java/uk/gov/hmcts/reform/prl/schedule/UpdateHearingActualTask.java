package uk.gov.hmcts.reform.prl.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.services.UpdateHearingActualsService;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateHearingActualTask implements Runnable {

    private final UpdateHearingActualsService updateHearingActualsService;

    @Override
    public void run() {
        //Invoke fm5 reminder service to evaluate & notify if needed
        updateHearingActualsService.updateHearingActuals();
    }
}
