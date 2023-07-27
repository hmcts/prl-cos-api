package uk.gov.hmcts.reform.prl.exception;

public class ServiceOfApplicationException extends RuntimeException {
    public ServiceOfApplicationException(String message) {
        super(message);
    }

    public ServiceOfApplicationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
