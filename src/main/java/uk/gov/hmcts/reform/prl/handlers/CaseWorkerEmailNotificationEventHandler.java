package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.events.CaseWorkerNotificationEmailEvent;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseWorkerEmailNotificationEventHandler {

    private final CaseWorkerEmailService caseWorkerEmailService;

    @EventListener(condition = "#event.typeOfEvent eq 'Notify court'")
    public void notifyLocalCourt(final CaseWorkerNotificationEmailEvent event) {
        caseWorkerEmailService.sendEmailToFl401LocalCourt(
            event.getCaseDetailsModel(),
            event.getCourtEmailAddress()
        );
    }

    @EventListener(condition = "#event.typeOfEvent eq 'Resubmit email'")
    public void notifyCaseWorkerForCaseResubmission(final CaseWorkerNotificationEmailEvent event) {
        caseWorkerEmailService.sendEmail(event.getCaseDetailsModel());
    }

    @EventListener(condition = "#event.typeOfEvent eq 'Notify court admin'")
    public void notifyCourtAdmin(final CaseWorkerNotificationEmailEvent event) {
        caseWorkerEmailService.sendEmailToCourtAdmin(event.getCaseDetailsModel());
    }

    @EventListener(condition = "#event.typeOfEvent eq 'Return application'")
    public void notifySolicitorForReturnApplication(final CaseWorkerNotificationEmailEvent event) {
        caseWorkerEmailService.sendReturnApplicationEmailToSolicitor(event.getCaseDetailsModel());
    }

    @EventListener(condition = "#event.typeOfEvent eq 'Notify case withdrawal local court'")
    public void notifyLocalCourtForCaseWithdrawal(final CaseWorkerNotificationEmailEvent event) {
        caseWorkerEmailService
            .sendWithdrawApplicationEmailToLocalCourt(event.getCaseDetailsModel(), event.getCourtEmailAddress());
    }
}
