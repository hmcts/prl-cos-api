package uk.gov.hmcts.reform.prl.exception.cafcass.exceptionhandlers;

import com.sun.jdi.InternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.prl.exception.cafcass.ForbiddenException;
import uk.gov.hmcts.reform.prl.exception.cafcass.UnauthorizedException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResponseExceptionHandlerTest {

    ResponseExceptionHandler responseExceptionHandler;

    @BeforeEach
    void setUp() {
        responseExceptionHandler = new ResponseExceptionHandler();
    }

    @Test
    void handleInvalidTokenException() {
        assertNotNull(
            responseExceptionHandler.handleInvalidTokenException(
                new InvalidTokenException("Test Exception")));
    }

    @Test
    void handleUnAuthorizedException() {
        assertNotNull(
            responseExceptionHandler.handleUnAuthorizedException(
                new UnauthorizedException("Test Exception")));
    }

    @Test
    void handleForbiddenException() {
        assertNotNull(
            responseExceptionHandler.handleForbiddenException(
                new ForbiddenException("Test Exception")));
    }

    @Test
    void handleInternalException() {
        assertNotNull(
            responseExceptionHandler.handleInternalException(
                new InternalException("Test Exception")));
    }



}


