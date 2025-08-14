package uk.gov.hmcts.reform.prl.exception;

public class InvalidPartyIdException extends RuntimeException {
    public InvalidPartyIdException(String message) {
        super(message);
    }
}
