package uk.gov.hmcts.reform.prl.services.sendandreply;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.services.sendandreply.messagehandler.MessageHandler;
import uk.gov.hmcts.reform.prl.services.sendandreply.messagehandler.MessageRequest;
import uk.gov.hmcts.reform.prl.services.sendandreply.messagehandler.SendAndReplyMessageHandlerService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SendAndReplyMessageHandlerServiceTest {

    @Test
    void testHandleMessage() {
        MessageHandler handler1 = mockMessageHandler(false);
        MessageHandler handler2 = mockMessageHandler(true);
        MessageHandler handler3 = mockMessageHandler(false);
        MessageHandler handler4 = mockMessageHandler(true);

        SendAndReplyMessageHandlerService service = new SendAndReplyMessageHandlerService(
            List.of(handler1, handler2, handler3, handler4)
        );
        service.handleMessage(mock(MessageRequest.class));

        // Verify that only the handlers that can handle the message are invoked
        verify(handler1, never()).handle(any(MessageRequest.class));
        verify(handler2).handle(any(MessageRequest.class));
        verify(handler3, never()).handle(any(MessageRequest.class));
        verify(handler4).handle(any(MessageRequest.class));
    }

    private MessageHandler mockMessageHandler(boolean canHandle) {
        MessageHandler messageHandler = mock(MessageHandler.class);
        when(messageHandler.canHandle(any(MessageRequest.class))).thenReturn(canHandle);
        return messageHandler;
    }
}
