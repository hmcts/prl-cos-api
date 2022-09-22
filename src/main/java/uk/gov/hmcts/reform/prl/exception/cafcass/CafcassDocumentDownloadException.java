package uk.gov.hmcts.reform.prl.exception.cafcass;

public class CafcassDocumentDownloadException extends RuntimeException {
    private static final long serialVersionUID = 1242994124480111078L;

    public CafcassDocumentDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
