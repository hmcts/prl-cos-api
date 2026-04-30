package uk.gov.hmcts.reform.prl.services.sendgrid.logs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.clients.SendGridEmailLogsClient;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLog;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLogMessage;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLogsRequest;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridLogsResponse;
import uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs.SendGridMessageResponse;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {
    @Mock
    private SendGridEmailLogsClient client;

    @InjectMocks
    private MessageService messageService;

    private SendGridLog log;
    private SendGridLogsResponse logsResponse;
    private SendGridMessageResponse messageResponse;

    @BeforeEach
    void setUp() {
        log = SendGridLog.builder()
                .fromEmail("from@test.com")
                .toEmail("to@test.com")
                .sgMessageId("msgid123")
                .subject("subject")
                .status("delivered")
                .build();
        logsResponse = SendGridLogsResponse.builder()
                .messages(Collections.singletonList(log))
                .build();
        messageResponse = SendGridMessageResponse.builder()
                .fromEmail("from@test.com")
                .toEmail("to@test.com")
                .sgMessageId("msgid123")
                .subject("subject")
                .status("delivered")
                .build();
    }

    @Test
    void getMessages_shouldReturnMappedMessages() {
        when(client.getLogs(any(SendGridLogsRequest.class))).thenReturn(logsResponse);
        when(client.getMessage("msgid123")).thenReturn(messageResponse);

        List<SendGridLogMessage> result = messageService.getMessages("status='delivered'");
        assertThat(result).hasSize(1);
        SendGridLogMessage msg = result.getFirst();
        assertThat(msg.getSendGridLog()).isEqualTo(log);
        assertThat(msg.getSendGridMessageResponse()).isEqualTo(messageResponse);
        verify(client).getLogs(any(SendGridLogsRequest.class));
        verify(client).getMessage("msgid123");
    }

    @Test
    void getMessages_withLimit_shouldReturnMappedMessages() {
        when(client.getLogs(any(SendGridLogsRequest.class))).thenReturn(logsResponse);
        when(client.getMessage("msgid123")).thenReturn(messageResponse);

        List<SendGridLogMessage> result = messageService.getMessages("status='delivered'", 1);
        assertThat(result).hasSize(1);
        verify(client).getLogs(any(SendGridLogsRequest.class));
        verify(client).getMessage("msgid123");
    }

    @Test
    void getMessages_withInvalidLimit_shouldThrowException() {
        assertThatThrownBy(() -> messageService.getMessages("status='delivered'", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Limit must be between 1 and 1000");
        assertThatThrownBy(() -> messageService.getMessages("status='delivered'", 1001))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Limit must be between 1 and 1000");
    }
}
