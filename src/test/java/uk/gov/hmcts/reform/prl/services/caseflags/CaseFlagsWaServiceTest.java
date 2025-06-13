package uk.gov.hmcts.reform.prl.services.caseflags;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.services.EventService;

@ExtendWith(MockitoExtension.class)
class CaseFlagsWaServiceTest {

    @Mock
    private EventService eventPublisher;

    @InjectMocks
    private CaseFlagsWaService caseFlagsWaService;


    @Test
    void testSetUpWaTaskForCaseFlagsEventHandler() {
        caseFlagsWaService.setUpWaTaskForCaseFlagsEventHandler("auth-token", CallbackRequest.builder().build());
        Mockito.verify(eventPublisher,Mockito.times(1)).publishEvent(Mockito.any());
    }
}
