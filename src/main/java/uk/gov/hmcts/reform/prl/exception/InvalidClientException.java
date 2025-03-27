package uk.gov.hmcts.reform.prl.exception;

public class InvalidClientException extends RuntimeException {

    public InvalidClientException(String message) {
        super(message);
    }
}
