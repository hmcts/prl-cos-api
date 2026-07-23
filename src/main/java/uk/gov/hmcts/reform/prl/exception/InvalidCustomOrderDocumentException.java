package uk.gov.hmcts.reform.prl.exception;

/**
 * Thrown when a user-uploaded custom order document is not a valid .docx file
 * (e.g. a PDF renamed to .docx). Callers should clear customOrderDoc and
 * return an error to the user instead of continuing the event.
 */
public class InvalidCustomOrderDocumentException extends RuntimeException {

    public InvalidCustomOrderDocumentException(String message) {
        super(message);
    }

    public InvalidCustomOrderDocumentException(String message, Throwable cause) {
        super(message, cause);
    }
}

