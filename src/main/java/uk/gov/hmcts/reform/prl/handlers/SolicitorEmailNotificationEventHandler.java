package uk.gov.hmcts.reform.prl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.events.SolicitorNotificationEmailEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.C100JsonMapper;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SolicitorEmailNotificationEventHandler {

    private final SolicitorEmailService solicitorEmailService;
    private final ObjectMapper objectMapper;
    private final SendgridService sendgridService;
    private final C100JsonMapper c100JsonMapper;

    @EventListener(condition = "#event.typeOfEvent eq 'awaiting payment'")
    public void notifySolicitorForAwaitingPayment(final SolicitorNotificationEmailEvent event) {
        solicitorEmailService.sendAwaitingPaymentEmail(event.getCaseDetails());
    }

    @EventListener(condition = "#event.typeOfEvent eq 'fl401 notification'")
    public void notifyFl401Solicitor(final SolicitorNotificationEmailEvent event) {
        solicitorEmailService.sendEmailToFl401Solicitor(event.getCaseDetailsModel(),
                                                        event.getUserDetails());
    }

    @EventListener(condition = "#event.typeOfEvent eq 'Resubmit email'")
    public void notifySolicitorForCaseResubmission(final SolicitorNotificationEmailEvent event) {
        solicitorEmailService.sendReSubmitEmail(event.getCaseDetailsModel());
    }

    @EventListener(condition = "#event.typeOfEvent eq 'RPA notification'")
    public void notifyRPaForCaseIssuance(final SolicitorNotificationEmailEvent event) throws IOException {
        CaseData caseData = CaseUtils.getCaseData(event.getCaseDetailsModel(), objectMapper);
        requireNonNull(caseData);
        sendgridService.sendEmail(c100JsonMapper.map(caseData));
    }

    @EventListener(condition = "#event.typeOfEvent eq 'C100 case withdraw'")
    public void notifyC100SolicitorOfCaseWithdrawal(final SolicitorNotificationEmailEvent event) {
        solicitorEmailService
            .sendWithDrawEmailToSolicitorAfterIssuedState(event.getCaseDetailsModel(), event.getUserDetails());
    }

    @EventListener(condition = "#event.typeOfEvent eq 'Fl401 case withdraw'")
    public void notifyFL401SolicitorOfCaseWithdrawal(final SolicitorNotificationEmailEvent event) {
        solicitorEmailService
            .sendWithDrawEmailToFl401SolicitorAfterIssuedState(event.getCaseDetailsModel(), event.getUserDetails());
    }

    @EventListener(condition = "#event.typeOfEvent eq 'C100 case withdraw before issue'")
    public void notifyC100SolicitorOfCaseWithdrawalBeforeIssue(final SolicitorNotificationEmailEvent event) {
        solicitorEmailService.sendWithDrawEmailToSolicitor(event.getCaseDetailsModel(), event.getUserDetails());
    }

    @EventListener(condition = "#event.typeOfEvent eq 'Fl401 case withdraw before issue'")
    public void notifyFL401SolicitorOfCaseWithdrawalBeforeIssue(final SolicitorNotificationEmailEvent event) {
        solicitorEmailService.sendWithDrawEmailToFl401Solicitor(event.getCaseDetailsModel(), event.getUserDetails());
    }
}
