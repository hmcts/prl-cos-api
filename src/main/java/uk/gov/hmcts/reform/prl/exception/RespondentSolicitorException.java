package uk.gov.hmcts.reform.prl.exception;

public class RespondentSolicitorException extends RuntimeException {
    public RespondentSolicitorException(String message) {
        super(message);
    }

    public RespondentSolicitorException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
