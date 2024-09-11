package uk.gov.hmcts.reform.prl.services.caseflags;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.events.CaseFlagsEvent;
import uk.gov.hmcts.reform.prl.services.EventService;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CaseFlagsWaService {
    private final EventService eventPublisher;

    public void setUpWaTaskForCaseFlagsEventHandler(String authorisation, CallbackRequest callbackRequest) {
        CaseFlagsEvent caseFlagsEvent = CaseFlagsEvent.builder()
            .authorisation(authorisation)
            .callbackRequest(callbackRequest)
            .build();
        eventPublisher.publishEvent(caseFlagsEvent);
    }
}
