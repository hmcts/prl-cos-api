package uk.gov.hmcts.reform.prl.exception;

public class ManageOrderRuntimeException extends IllegalArgumentException {
    public ManageOrderRuntimeException(String message) {
        super(message);
    }

    public ManageOrderRuntimeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
