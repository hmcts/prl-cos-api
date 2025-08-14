package uk.gov.hmcts.reform.prl.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.prl.framework.exceptions.DocumentGenerationException;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ERROR_MESSAGE = "An error occurred while processing your request: ";

    @ExceptionHandler(ManageOrderRuntimeException.class)
    public ResponseEntity<ErrorResponse> handleManageOrderRuntimeException(ManageOrderRuntimeException ex) {
        log.error("Exception occurred while Manage Orders due to: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder(ex, ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500),
                                                                                         ex.getMessage())).build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DocumentGenerationException.class)
    public ResponseEntity<ErrorResponse> handleDocumentGenerationException(DocumentGenerationException ex) {
        log.error("Exception occurred while document generation due to: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder(ex, ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500),
                                                                                         ex.getMessage())).build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidClientException.class)
    public ResponseEntity<ErrorResponse> handleInvalidClientException(InvalidClientException ex) {
        log.error("Exception occurred while validating the client: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder(ex, ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500),
                                                                                         ex.getMessage())).build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ManageOrdersUnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleManageOrdersUnsupportedOperationException(ManageOrdersUnsupportedOperationException ex) {
        log.error("Unsupported operation during manage orders due to: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder(ex, ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500),
                                                                                         ex.getMessage())).build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoCaseDataFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoCaseDataFoundException(NoCaseDataFoundException ex) {
        log.error("No Case Data found due to: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder(ex, ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500),
                                                                                         ex.getMessage())).build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(GovUkNotificationException.class)
    public ResponseEntity<ErrorResponse> handleGovUkNotificationException(GovUkNotificationException ex) {
        log.error("Exception occurred while sending email notification due to: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder(ex, ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500),
            ex.getMessage())).build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BulkPrintException.class)
    public ResponseEntity<ErrorResponse> handleBulkPrintException(BulkPrintException ex) {
        log.error("Exception occurred during bulk print due to: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder(ex, ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500),
                ex.getMessage())).build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
