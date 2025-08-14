package uk.gov.hmcts.reform.prl.exception;

public class GovUkNotificationException extends RuntimeException {
    public GovUkNotificationException(String message, Throwable e) {
        super(message, e);
    }
}
