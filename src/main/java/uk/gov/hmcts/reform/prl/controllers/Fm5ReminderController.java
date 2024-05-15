package uk.gov.hmcts.reform.prl.controllers;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.services.Fm5ReminderService;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/fm5")
public class Fm5ReminderController {

    private final Fm5ReminderService fm5ReminderService;

    /**
     * TEMP API TO TEST FM5 REMINDER NOTIFICATIONS.
     */
    @PostMapping("/reminder-notifications/{hearingAwayDays}")
    public void sendFm5ReminderNotifications(@PathVariable("hearingAwayDays") Long hearingAwayDays) {
        log.info("*** Trigger FM5 reminder notifications via API ***");
        fm5ReminderService.sendFm5ReminderNotifications(hearingAwayDays);
    }
}
