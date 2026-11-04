package uk.gov.hmcts.reform.prl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.events.SolicitorNotificationEmailEvent;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;



@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class SolicitorEmailNotificationEventHandlerTest {
    @Mock
    private SolicitorEmailService solicitorEmailService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private SendgridService sendgridService;

    @InjectMocks
    private SolicitorEmailNotificationEventHandler solicitorEmailNotificationEventHandler;

    private SolicitorNotificationEmailEvent solicitorNotificationEmailEvent;

    @Before
    public void init() {
        solicitorNotificationEmailEvent
            = SolicitorNotificationEmailEvent.builder()
            .caseDetailsModel(CaseDetails.builder().build())
            .caseDetails(uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().build())
            .userDetails(UserDetails.builder().build())
            .build();
    }

    @Test
    public void shouldNotifySolicitorForAwaitingPayment() {
        solicitorEmailNotificationEventHandler
            .notifySolicitorForAwaitingPayment(solicitorNotificationEmailEvent);
        Mockito.verify(solicitorEmailService,Mockito.times(1))
            .sendAwaitingPaymentEmail(solicitorNotificationEmailEvent.getCaseDetails());
    }

    @Test
    public void shouldNotifyFl401Solicitor() {
        solicitorEmailNotificationEventHandler
            .notifyFl401Solicitor(solicitorNotificationEmailEvent);
        Mockito.verify(solicitorEmailService,Mockito.times(1))
            .sendEmailToFl401Solicitor(solicitorNotificationEmailEvent.getCaseDetailsModel(),
                                       solicitorNotificationEmailEvent.getUserDetails());
    }

    @Test
    public void shouldNotifySolicitorForCaseResubmission() {
        solicitorEmailNotificationEventHandler
            .notifySolicitorForCaseResubmission(solicitorNotificationEmailEvent);
        Mockito.verify(solicitorEmailService,Mockito.times(1))
            .sendReSubmitEmail(solicitorNotificationEmailEvent.getCaseDetailsModel());
    }

    @Test
    public void shouldNotifyC100SolicitorOfCaseWithdrawal() {
        solicitorEmailNotificationEventHandler
            .notifyC100SolicitorOfCaseWithdrawal(solicitorNotificationEmailEvent);
        Mockito.verify(solicitorEmailService,Mockito.times(1))
            .sendWithDrawEmailToSolicitorAfterIssuedState(solicitorNotificationEmailEvent.getCaseDetailsModel(),
                                                          solicitorNotificationEmailEvent.getUserDetails());
    }

    @Test
    public void shouldNotifyFL401SolicitorOfCaseWithdrawal() {
        solicitorEmailNotificationEventHandler
            .notifyFL401SolicitorOfCaseWithdrawal(solicitorNotificationEmailEvent);
        Mockito.verify(solicitorEmailService,Mockito.times(1))
            .sendWithDrawEmailToFl401SolicitorAfterIssuedState(solicitorNotificationEmailEvent.getCaseDetailsModel(),
                                                          solicitorNotificationEmailEvent.getUserDetails());
    }

    @Test
    public void shouldNotifyC100SolicitorOfCaseWithdrawalBeforeIssue() {
        solicitorEmailNotificationEventHandler
            .notifyC100SolicitorOfCaseWithdrawalBeforeIssue(solicitorNotificationEmailEvent);
        Mockito.verify(solicitorEmailService,Mockito.times(1))
            .sendWithDrawEmailToSolicitor(solicitorNotificationEmailEvent.getCaseDetailsModel(),
                                                               solicitorNotificationEmailEvent.getUserDetails());
    }

    @Test
    public void shouldNotifyFL401SolicitorOfCaseWithdrawalBeforeIssue() {
        solicitorEmailNotificationEventHandler
            .notifyFL401SolicitorOfCaseWithdrawalBeforeIssue(solicitorNotificationEmailEvent);
        Mockito.verify(solicitorEmailService,Mockito.times(1))
            .sendWithDrawEmailToFl401Solicitor(solicitorNotificationEmailEvent.getCaseDetailsModel(),
                                          solicitorNotificationEmailEvent.getUserDetails());
    }

}
