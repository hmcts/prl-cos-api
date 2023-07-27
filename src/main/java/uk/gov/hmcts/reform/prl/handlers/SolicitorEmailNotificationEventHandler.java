package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.prl.events.SolicitorNotificationEmailEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangeContentProvider;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;

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
