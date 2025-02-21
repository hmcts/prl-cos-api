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
}
