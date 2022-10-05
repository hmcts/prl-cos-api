package uk.gov.hmcts.reform.prl.exception.cafcass.exceptionhandlers;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.prl.exception.cafcass.ForbiddenException;
import uk.gov.hmcts.reform.prl.exception.cafcass.UnauthorizedException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.status;

@ControllerAdvice
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ResponseExceptionHandler.class);

    @ExceptionHandler(InvalidTokenException.class)
    protected ResponseEntity<ApiError> handleInvalidTokenException(InvalidTokenException exc) {
        log.warn(exc.getMessage(), exc);
        return status(UNAUTHORIZED).body(new ApiError(exc.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    protected ResponseEntity<ApiError> handleUnAuthorizedException(
            UnauthorizedException exception) {
        log.error(exception.getMessage(), exception);

        return status(UNAUTHORIZED).body(new ApiError(exception.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    protected ResponseEntity<ApiError> handleForbiddenException(ForbiddenException exception) {
        log.error(exception.getMessage(), exception);

        return status(FORBIDDEN).body(new ApiError(exception.getMessage()));
    }

    @ExceptionHandler(FeignException.class)
    protected ResponseEntity<String> handleFeignException(FeignException exception) {
        log.error(exception.getMessage(), exception);

        return status(exception.status()).body(exception.getMessage());
    }

    @ExceptionHandler({
        Exception.class,
    })
    protected ResponseEntity<ApiError> handleInternalException(
            Exception exception) {
        log.error(exception.getMessage(), exception);

        return status(INTERNAL_SERVER_ERROR).body(new ApiError(exception.getMessage()));
    }
}
