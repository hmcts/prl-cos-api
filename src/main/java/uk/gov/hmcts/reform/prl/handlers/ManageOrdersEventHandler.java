package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.events.ManageOrderNotificationsEvent;
import uk.gov.hmcts.reform.prl.services.ManageOrderEmailService;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageOrdersEventHandler {
    private final ManageOrderEmailService manageOrderEmailService;

    @EventListener(condition = "#event.typeOfEvent eq 'Manage Order Notifications'")
    public void notifyPartiesOrSolicitor(final ManageOrderNotificationsEvent event) {
        //notify party or solicitor
        manageOrderEmailService.sendEmailWhenOrderIsServed(event.getCaseDetails());

    }
}
