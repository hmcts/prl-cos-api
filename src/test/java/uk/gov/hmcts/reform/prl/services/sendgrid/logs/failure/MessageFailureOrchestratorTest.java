package uk.gov.hmcts.reform.prl.services.sendgrid.logs.failure;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLogMessage;
import uk.gov.hmcts.reform.prl.models.sendgrid.logs.MessageFailureView;
import uk.gov.hmcts.reform.prl.services.sendgrid.logs.MessageService;
import uk.gov.hmcts.reform.prl.services.sendgrid.logs.failure.processing.MessageFailureMapper;
import uk.gov.hmcts.reform.prl.services.sendgrid.logs.failure.report.MessageFailureHandler;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageFailureOrchestratorTest {

    @Mock
    private MessageService messageService;
    @Mock
    private MessageFailureMapper messageFailureMapper;
    @Mock
    private MessageFailureHandler messageFailureHandler;

    @InjectMocks
    private MessageFailureOrchestrator orchestrator;

    @Test
    void shouldReturnEarlyWhenNoMessages() {
        when(messageService.getMessages(any(String.class))).thenReturn(Collections.emptyList());

        orchestrator.processQuery("query");

        verify(messageService).getMessages("query");
        verifyNoInteractions(messageFailureMapper);
        verifyNoInteractions(messageFailureHandler);
    }

    @Test
    void shouldMapAndHandleWhenMessagesPresent() {
        SendGridLogMessage logMessage = mock(SendGridLogMessage.class);
        List<SendGridLogMessage> logMessages = List.of(logMessage);
        List<MessageFailureView> failureViews = List.of(mock(MessageFailureView.class));

        when(messageService.getMessages(any(String.class))).thenReturn(logMessages);
        when(messageFailureMapper.convertToEmailFailureView(logMessages)).thenReturn(failureViews);

        orchestrator.processQuery("query");

        verify(messageService).getMessages("query");
        verify(messageFailureMapper).convertToEmailFailureView(logMessages);
        verify(messageFailureHandler).handle(failureViews);
    }
}
