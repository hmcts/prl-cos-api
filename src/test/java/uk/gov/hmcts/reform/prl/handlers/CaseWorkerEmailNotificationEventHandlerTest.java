package uk.gov.hmcts.reform.prl.handlers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.events.CaseWorkerNotificationEmailEvent;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class CaseWorkerEmailNotificationEventHandlerTest {
    @Mock
    private CaseWorkerEmailService caseWorkerEmailService;

    @InjectMocks
    private CaseWorkerEmailNotificationEventHandler caseWorkerEmailNotificationEventHandler;

    private CaseWorkerNotificationEmailEvent caseWorkerNotificationEmailEvent;

    @BeforeEach
    void init() {
        caseWorkerNotificationEmailEvent
            = CaseWorkerNotificationEmailEvent.builder()
            .caseDetailsModel(CaseDetails.builder().build())
            .courtEmailAddress("testcourt@test.com")
            .build();
    }

    @Test
    void shouldNotifyLocalCourt() {
        caseWorkerEmailNotificationEventHandler.notifyLocalCourt(caseWorkerNotificationEmailEvent);
        Mockito.verify(caseWorkerEmailService, Mockito.times(1))
            .sendEmailToFl401LocalCourt(caseWorkerNotificationEmailEvent.getCaseDetailsModel(),
                                        caseWorkerNotificationEmailEvent.getCourtEmailAddress());
    }

    @Test
    void shouldNotifyCaseWorkerForCaseReSubmission() {
        caseWorkerEmailNotificationEventHandler.notifyCaseWorkerForCaseResubmission(caseWorkerNotificationEmailEvent);
        Mockito.verify(caseWorkerEmailService, Mockito.times(1))
            .sendEmail(caseWorkerNotificationEmailEvent.getCaseDetailsModel());
    }

    @Test
    void shouldNotifyCourtAdmin() {
        caseWorkerEmailNotificationEventHandler.notifyCourtAdmin(caseWorkerNotificationEmailEvent);
        Mockito.verify(caseWorkerEmailService, Mockito.times(1))
            .sendEmailToCourtAdmin(caseWorkerNotificationEmailEvent.getCaseDetailsModel());
    }

    @Test
    void shouldNotifySolicitorForReturnApplication() {
        caseWorkerEmailNotificationEventHandler.notifySolicitorForReturnApplication(caseWorkerNotificationEmailEvent);
        Mockito.verify(caseWorkerEmailService, Mockito.times(1))
            .sendReturnApplicationEmailToSolicitor(caseWorkerNotificationEmailEvent.getCaseDetailsModel());
    }

    @Test
    void notifyLocalCourtForCaseWithdrawal() {
        caseWorkerEmailNotificationEventHandler.notifyLocalCourtForCaseWithdrawal(caseWorkerNotificationEmailEvent);
        Mockito.verify(caseWorkerEmailService, Mockito.times(1))
            .sendWithdrawApplicationEmailToLocalCourt(caseWorkerNotificationEmailEvent.getCaseDetailsModel(),
                                                      caseWorkerNotificationEmailEvent.getCourtEmailAddress());
    }
}
