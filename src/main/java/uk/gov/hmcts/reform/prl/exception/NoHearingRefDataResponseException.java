package uk.gov.hmcts.reform.prl.exception;

public class NoHearingRefDataResponseException extends RuntimeException {
    public NoHearingRefDataResponseException(String message) {
        super(message);
    }

    public NoHearingRefDataResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
