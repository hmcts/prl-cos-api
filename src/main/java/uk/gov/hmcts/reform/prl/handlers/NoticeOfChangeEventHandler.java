package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangeContentProvider;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfChangeEventHandler {
    private final EmailService emailService;
    private final NoticeOfChangeContentProvider noticeOfChangeContentProvider;

    @EventListener
    public void notifyLegalRepresentative(final NoticeOfChangeEvent event) {
        CaseData caseData = event.getCaseData();
        emailService.send(
            event.getSolicitorEmailAddress(),
            EmailTemplateNames.CA_DA_RESPONDENT_SOLICITOR_NOC,
            noticeOfChangeContentProvider.buildNoticeOfChangeEmail(caseData, event.getSolicitorName()),
            LanguagePreference.english
        );
    }
}
