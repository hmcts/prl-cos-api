package uk.gov.hmcts.reform.prl.services.sendgrid.logs.failure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLogMessage;
import uk.gov.hmcts.reform.prl.models.sendgrid.logs.MessageFailureView;
import uk.gov.hmcts.reform.prl.services.sendgrid.logs.MessageService;
import uk.gov.hmcts.reform.prl.services.sendgrid.logs.failure.processing.MessageFailureMapper;
import uk.gov.hmcts.reform.prl.services.sendgrid.logs.failure.report.MessageFailureHandler;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageFailureOrchestrator {

    private final MessageService messageService;
    private final MessageFailureMapper messageFailureMapper;
    private final MessageFailureHandler messageFailureHandler;

    public void processQuery(String query) {
        List<SendGridLogMessage> sendLogMessages = messageService.getMessages(query);
        log.info("Message failures: {}", sendLogMessages.size());
        if (sendLogMessages.isEmpty()) {
            return;
        }

        List<MessageFailureView> failures = messageFailureMapper.convertToEmailFailureView(sendLogMessages);

        messageFailureHandler.handle(failures);
    }
}
