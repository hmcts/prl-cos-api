package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.events.BarristerChangeEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangeContentProvider;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BarristerChangeEventHandler {
    private final EmailService emailService;
    private final NoticeOfChangeContentProvider noticeOfChangeContentProvider;

    @Async
    @EventListener(condition = "#event.typeOfEvent.displayedValue eq 'Add Barrister'")
    public void notifyBarrister(final BarristerChangeEvent event) {
        CaseData caseData = event.getCaseData();
        sendEmailToBarrister(caseData, event, EmailTemplateNames.CA_DA_SOLICITOR_NOC);
    }

    private void sendEmailToBarrister(CaseData caseData, BarristerChangeEvent event, EmailTemplateNames emailTemplateName) {
        if (null != event.getAllocatedBarrister().getBarristerEmail()) {
            log.info("Sending Barrister email on case id {}", caseData.getId());
            emailService.send(
                event.getAllocatedBarrister().getBarristerEmail(),
                emailTemplateName,
                noticeOfChangeContentProvider.buildNocEmailSolicitor(caseData, event.getAllocatedBarrister().getBarristerFirstName()),
                LanguagePreference.getPreferenceLanguage(caseData)
            );
        } else {
            log.info(
                "Unable to send email to Barrister as the they don't have any email address for case id {}",
                caseData.getId()
            );
        }
    }

    @Async
    @EventListener(condition = "#event.typeOfEvent.displayedValue eq 'Remove Barrister'")
    public void notifyWhenBarristerRemoved(final BarristerChangeEvent event) {
        CaseData caseData = event.getCaseData();
        sendEmailToBarrister(caseData, event, EmailTemplateNames.CA_DA_REMOVE_SOLICITOR_NOC);
    }
}
