package uk.gov.hmcts.reform.prl.services.caseflags;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.services.EventService;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseFlagsWaServiceTest {

    @Mock
    private EventService eventPublisher;

    @InjectMocks
    private CaseFlagsWaService caseFlagsWaService;


    @Test
    public void testSetUpWaTaskForCaseFlagsEventHandler() {
        caseFlagsWaService.setUpWaTaskForCaseFlagsEventHandler("auth-token", CallbackRequest.builder().build());
        Mockito.verify(eventPublisher,Mockito.times(1)).publishEvent(Mockito.any());
    }
}
