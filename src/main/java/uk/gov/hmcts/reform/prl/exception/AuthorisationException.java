package uk.gov.hmcts.reform.prl.exception;

public class AuthorisationException extends RuntimeException {

    public AuthorisationException(String ex) {
        super(ex);
    }
}
