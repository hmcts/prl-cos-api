package uk.gov.hmcts.reform.prl.exception;

public class PdfConversionException  extends RuntimeException {
    public PdfConversionException(String message) {
        super(message);
    }

    public PdfConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
