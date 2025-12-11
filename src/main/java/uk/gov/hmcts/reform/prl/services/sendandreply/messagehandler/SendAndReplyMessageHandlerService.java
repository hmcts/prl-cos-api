package uk.gov.hmcts.reform.prl.services.sendandreply.messagehandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SendAndReplyMessageHandlerService {

    private final List<MessageHandler> messageHandlers;

    public void handleMessage(MessageRequest messageRequest) {
        messageHandlers.stream()
            .filter(handler -> handler.canHandle(messageRequest))
            .forEach(handler -> handler.handle(messageRequest));
    }
}
