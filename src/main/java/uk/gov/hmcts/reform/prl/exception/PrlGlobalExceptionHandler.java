package uk.gov.hmcts.reform.prl.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.gov.hmcts.reform.prl.framework.exceptions.DocumentGenerationException;

@RestControllerAdvice
@Slf4j
public class PrlGlobalExceptionHandler {

    @ExceptionHandler(DocumentGenerationException.class)
    public void handleDocumentGenerationException(DocumentGenerationException ex) {
        log.error("Exception occurred: {}", ex.getMessage());
    }

    @ExceptionHandler(ManageOrderRuntimeException.class)
    public void handleManageOrderRuntimeException(ManageOrderRuntimeException ex) {
        log.error("Exception occurred: {}", ex.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException ex) {
        log.error("Exception occurred: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder(ex, ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500),
                                                                                         ex.getMessage())).build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
