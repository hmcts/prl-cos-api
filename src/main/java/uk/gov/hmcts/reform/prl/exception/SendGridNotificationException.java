package uk.gov.hmcts.reform.prl.exception;

public class SendGridNotificationException extends RuntimeException {
    public SendGridNotificationException(String message) {
        super(message);
    }
}
