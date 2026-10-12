package uk.gov.hmcts.reform.prl.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.services.requestorder.RequestOrderTaskService;

@Component
@Slf4j
@RequiredArgsConstructor
public class RequestOrderTask implements Runnable {

    private final RequestOrderTaskService requestOrderTaskService;

    @Override
    public void run() {
        requestOrderTaskService.processRequestOrderTasks();
    }
}
