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
import uk.gov.hmcts.reform.prl.events.CaseWorkerNotificationEmailEvent;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;

@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class CaseWorkerEmailNotificationEventHandlerTest {
    @Mock
    private CaseWorkerEmailService caseWorkerEmailService;

    @InjectMocks
    private CaseWorkerEmailNotificationEventHandler caseWorkerEmailNotificationEventHandler;

    private CaseWorkerNotificationEmailEvent caseWorkerNotificationEmailEvent;

    @Before
    public void init() {
        caseWorkerNotificationEmailEvent
            = CaseWorkerNotificationEmailEvent.builder()
            .caseDetailsModel(CaseDetails.builder().build())
            .courtEmailAddress("testcourt@test.com")
            .build();
    }

    @Test
    public void shouldNotifyLocalCourt() {
        caseWorkerEmailNotificationEventHandler.notifyLocalCourt(caseWorkerNotificationEmailEvent);
        Mockito.verify(caseWorkerEmailService, Mockito.times(1))
            .sendEmailToFl401LocalCourt(caseWorkerNotificationEmailEvent.getCaseDetailsModel(),
                                        caseWorkerNotificationEmailEvent.getCourtEmailAddress());
    }

    @Test
    public void shouldNotifyCaseWorkerForCaseReSubmission() {
        caseWorkerEmailNotificationEventHandler.notifyCaseWorkerForCaseResubmission(caseWorkerNotificationEmailEvent);
        Mockito.verify(caseWorkerEmailService, Mockito.times(1))
            .sendEmail(caseWorkerNotificationEmailEvent.getCaseDetailsModel());
    }

    @Test
    public void shouldNotifyCourtAdmin() {
        caseWorkerEmailNotificationEventHandler.notifyCourtAdmin(caseWorkerNotificationEmailEvent);
        Mockito.verify(caseWorkerEmailService, Mockito.times(1))
            .sendEmailToCourtAdmin(caseWorkerNotificationEmailEvent.getCaseDetailsModel());
    }

    @Test
    public void shouldNotifySolicitorForReturnApplication() {
        caseWorkerEmailNotificationEventHandler.notifySolicitorForReturnApplication(caseWorkerNotificationEmailEvent);
        Mockito.verify(caseWorkerEmailService, Mockito.times(1))
            .sendReturnApplicationEmailToSolicitor(caseWorkerNotificationEmailEvent.getCaseDetailsModel());
    }

    @Test
    public void notifyLocalCourtForCaseWithdrawal() {
        caseWorkerEmailNotificationEventHandler.notifyLocalCourtForCaseWithdrawal(caseWorkerNotificationEmailEvent);
        Mockito.verify(caseWorkerEmailService, Mockito.times(1))
            .sendWithdrawApplicationEmailToLocalCourt(caseWorkerNotificationEmailEvent.getCaseDetailsModel(),
                                                      caseWorkerNotificationEmailEvent.getCourtEmailAddress());
    }
}
