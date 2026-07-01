package uk.gov.hmcts.reform.prl.exception;

public class HearingException extends IllegalArgumentException {
    public HearingException(String message) {
        super(message);
    }

    public HearingException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
