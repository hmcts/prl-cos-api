package uk.gov.hmcts.reform.prl.framework.exceptions;

public class DocumentGenerationException extends RuntimeException {

    public DocumentGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
