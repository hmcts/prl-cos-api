package uk.gov.hmcts.reform.prl.exception;

public class DocumentGenerationException extends RuntimeException {

    public DocumentGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
