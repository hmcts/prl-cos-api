package uk.gov.hmcts.reform.prl.exception.cafcass;

import java.io.Serializable;

public class PostcodeValidationException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 374299239041220L;

    public PostcodeValidationException(String errorMessage) {
        super(errorMessage);
    }

    public PostcodeValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
