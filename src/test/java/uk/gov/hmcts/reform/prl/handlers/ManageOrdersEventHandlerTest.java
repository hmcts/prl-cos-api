package uk.gov.hmcts.reform.prl.handlers;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.events.ManageOrderNotificationsEvent;
import uk.gov.hmcts.reform.prl.services.ManageOrderEmailService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class ManageOrdersEventHandlerTest {

    @Mock
    private ManageOrderEmailService manageOrderEmailService;

    @InjectMocks
    private ManageOrdersEventHandler manageOrdersEventHandler;

    private ManageOrderNotificationsEvent manageOrderNotificationsEvent;

    @Before
    public void init() {
        manageOrderNotificationsEvent = ManageOrderNotificationsEvent.builder()
            .typeOfEvent("")
            .caseDetails(CaseDetails.builder().build())
            .build();
    }

    @Test
    public void shouldNotifyLegalRepresentative() {

        manageOrdersEventHandler.notifyPartiesOrSolicitor(manageOrderNotificationsEvent);

        verify(manageOrderEmailService,times(1)).sendEmailWhenOrderIsServed(Mockito.any());

    }
}
