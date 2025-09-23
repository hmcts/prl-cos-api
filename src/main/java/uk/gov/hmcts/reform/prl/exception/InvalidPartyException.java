package uk.gov.hmcts.reform.prl.exception;

public class InvalidPartyException extends RuntimeException {
    public InvalidPartyException(String message) {
        super(message);
    }
}
