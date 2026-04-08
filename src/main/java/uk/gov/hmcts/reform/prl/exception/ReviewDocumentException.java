package uk.gov.hmcts.reform.prl.exception;

public class ReviewDocumentException  extends RuntimeException {
    public ReviewDocumentException(String message) {
        super(message);
    }

    public ReviewDocumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
