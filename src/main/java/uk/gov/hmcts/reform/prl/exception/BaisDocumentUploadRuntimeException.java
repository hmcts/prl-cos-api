package uk.gov.hmcts.reform.prl.exception;

public class BaisDocumentUploadRuntimeException extends IllegalArgumentException {
    public BaisDocumentUploadRuntimeException(String message) {
        super(message);
    }

    public BaisDocumentUploadRuntimeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
