package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.events.SolicitorNotificationEmailEvent;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SolicitorEmailNotificationEventHandler {

    private final SolicitorEmailService solicitorEmailService;

    @EventListener(condition = "#event.typeOfEvent eq 'awaiting payment'")
    public void notifySolicitorForAwaitingPayment(final SolicitorNotificationEmailEvent event) {
        solicitorEmailService.sendAwaitingPaymentEmail(event.getCaseDetails());
    }

    @EventListener(condition = "#event.typeOfEvent eq 'fl401 notification'")
    public void notifyFl401Solicitor(final SolicitorNotificationEmailEvent event) {
        solicitorEmailService.sendEmailToFl401Solicitor(event.getCaseDetailsModel(),
                                                        event.getUserDetails());
    }
}
