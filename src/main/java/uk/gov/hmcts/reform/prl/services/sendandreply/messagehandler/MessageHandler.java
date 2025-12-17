package uk.gov.hmcts.reform.prl.services.sendandreply.messagehandler;

/**
 * Generic interface for handling message requests.
 */
public interface MessageHandler {

    /**
     * Determines if this handler can process the given message request.
     *
     * @param messageRequest the message request to be evaluated
     * @return true if the handler can process the request, false otherwise
     */
    boolean canHandle(MessageRequest messageRequest);

    /**
     * Handles the given message request.
     *
     * @param messageRequest the message request to be processed
     */
    void handle(MessageRequest messageRequest);
}
