package uk.gov.hmcts.reform.prl.exception;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

public class InvalidClientException extends RuntimeException {

    public InvalidClientException() {
        super(INVALID_CLIENT);
    }

    public InvalidClientException(String message) {
        super(message);
    }
}
