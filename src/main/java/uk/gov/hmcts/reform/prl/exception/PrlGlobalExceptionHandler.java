package uk.gov.hmcts.reform.prl.exception;

import lombok.extern.slf4j.Slf4j;
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
}
