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
    public void notifySolicitorForAwaitingPayment(final CaseWorkerNotificationEmailEvent event) {
        caseWorkerEmailService.sendEmailToFl401LocalCourt(
            event.getCaseDetailsModel(),
            event.getCourtEmailAddress()
        );
    }
}
