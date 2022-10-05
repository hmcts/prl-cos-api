package uk.gov.hmcts.reform.prl.exception.cafcass;

public class UnauthorizedException extends RuntimeException {
    private static final long serialVersionUID = -948721106877408028L;

    public UnauthorizedException(String message) {
        super(message);
    }
}
