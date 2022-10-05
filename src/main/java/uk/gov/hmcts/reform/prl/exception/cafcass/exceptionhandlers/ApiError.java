package uk.gov.hmcts.reform.prl.exception.cafcass.exceptionhandlers;

public class ApiError {
    public final String error;

    public ApiError(String error) {
        this.error = error;
    }
}
