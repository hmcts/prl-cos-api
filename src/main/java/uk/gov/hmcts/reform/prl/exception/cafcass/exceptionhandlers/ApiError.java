package uk.gov.hmcts.reform.prl.exception.cafcass.exceptionhandlers;

import lombok.Getter;

@Getter
public class ApiError {
    private final String message;

    public ApiError(String error) {
        this.message = error;
    }
}
