package uk.gov.hmcts.reform.prl.exception;

public class EmptyFileException extends IllegalArgumentException {
    public EmptyFileException() {
        super("File cannot be empty");
    }
}
