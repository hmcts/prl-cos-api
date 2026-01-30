package uk.gov.hmcts.reform.prl.exception;

public class MissingCaseDataFieldException extends RuntimeException {

    public MissingCaseDataFieldException(String ex) {
        super(ex);
    }
}
