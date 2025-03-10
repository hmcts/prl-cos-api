package uk.gov.hmcts.reform.prl.exception;

public class NoStaffResponseException extends RuntimeException {
    public NoStaffResponseException(String message) {
        super(message);
    }

    public NoStaffResponseException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
