package uk.gov.hmcts.reform.prl.services.sendgrid.logs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.SendGridEmailLogsClient;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLog;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLogMessage;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLogsRequest;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLogsResponse;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridMessageResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {
    private static final int LIMIT_MAX = 1000;
    private static final int LIMIT_MIN = 1;

    private final SendGridEmailLogsClient client;

    public List<SendGridLogMessage> getMessages(String query) {
        return getMessages(query, LIMIT_MAX);
    }

    public List<SendGridLogMessage> getMessages(String query, int limit) {
        SendGridLogsResponse logsResponse = getLogs(query, limit);

        return logsResponse.getMessages().stream()
            .map(this::getLogMessage)
            .toList();
    }

    private SendGridLogsResponse getLogs(String query, int limit) {
        if (limit < LIMIT_MIN || limit > LIMIT_MAX) {
            throw new IllegalArgumentException("Limit must be between " + LIMIT_MIN + " and " + LIMIT_MAX);
        }

        SendGridLogsRequest request = SendGridLogsRequest.builder()
            .query(query)
            .limit(limit)
            .build();
        log.info("SendGrid logs request: {}", request);
        return client.getLogs(request);
    }

    private SendGridLogMessage getLogMessage(SendGridLog sendGridLog) {
        SendGridMessageResponse messageResponse = getMessage(sendGridLog.getSgMessageId());
        return SendGridLogMessage.builder()
            .sendGridLog(sendGridLog)
            .sendGridMessageResponse(messageResponse)
            .build();
    }

    private SendGridMessageResponse getMessage(String sgMessageId) {
        log.info("SendGrid message request: {}", sgMessageId);
        return client.getMessage(sgMessageId);
    }
}
