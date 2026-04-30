package uk.gov.hmcts.reform.prl.services.sendgrid.logs.failure.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.exception.SendGridNotificationException;
import uk.gov.hmcts.reform.prl.models.sendgrid.logs.MessageFailureView;
import uk.gov.hmcts.reform.prl.services.EmailTemplateService;
import uk.gov.hmcts.reform.prl.services.SendgridService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourtReportHandler implements MessageFailureHandler {

    private final EmailTemplateService emailTemplateService;
    private final SendgridService sendgridService;

    @Override
    public void handle(List<MessageFailureView> messageFailures) {
        messageFailures.stream()
            .collect(Collectors.groupingBy(MessageFailureView::getCourtName))
            .forEach((courtName, failureViews) -> {
                String messageIds = getMessageIds(failureViews);

                try {
                    String courtEmailAddress = getCourtEmailAddress(courtName);
                    String emailContent = createEmailContent(failureViews);
                    log.info("Sending email to {} for message ids {}", courtName, messageIds);
                    sendgridService.sendEmail(courtEmailAddress, "Email failures", emailContent);
                } catch (SendGridNotificationException | IOException e) {
                    log.error("Unable to send email to {} for message ids {}", courtName, messageIds, e);
                }
            });
    }

    private String createEmailContent(List<MessageFailureView> messageFailures) throws IOException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
        List<Map<String, String>> messageFailuresData = messageFailures.stream()
            .map(mfv -> Map.of(
                "caseReference", mfv.getCaseReference(),
                "toEmailAddress", mfv.getToEmailAddress(),
                "sentDate", dateFormatter.format(mfv.getSentDate()),
                "subject", mfv.getSubject(),
                "status", mfv.getStatus(),
                "reason", mfv.getReason(),
                "messageId", mfv.getMessageId(),
                "templateId", mfv.getTemplateId(),
                "templateName", mfv.getTemplateName()
            ))
            .toList();

        Map<String, Object> data = Map.of("failedEmails", messageFailuresData);

        return emailTemplateService.render("court-email-failure", data);
    }

    private String getMessageIds(List<MessageFailureView> failureViews) {
        return failureViews.stream()
            .map(MessageFailureView::getMessageId)
            .collect(Collectors.joining(", "));
    }

    private String getCourtEmailAddress(String courtName) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }
}
