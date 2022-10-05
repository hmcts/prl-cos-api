package uk.gov.hmcts.reform.prl.exception.cafcass.exceptionhandlers;

import com.sun.jdi.InternalException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.prl.exception.cafcass.ForbiddenException;
import uk.gov.hmcts.reform.prl.exception.cafcass.UnauthorizedException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ResponseExceptionHandlerTest {

    ResponseExceptionHandler responseExceptionHandler;

    @Test
    public  void handleInvalidTokenException() {
        responseExceptionHandler = new ResponseExceptionHandler();
        assertNotNull(
            responseExceptionHandler.handleInvalidTokenException(
                new InvalidTokenException("Test Exception")));
    }

    @Test
    public void handleUnAuthorizedException() {
        responseExceptionHandler = new ResponseExceptionHandler();

        assertNotNull(
            responseExceptionHandler.handleUnAuthorizedException(
                new UnauthorizedException("Test Exception")));
    }

    @Test
    public  void handleForbiddenException() {
        responseExceptionHandler = new ResponseExceptionHandler();

        assertNotNull(
            responseExceptionHandler.handleForbiddenException(
                new ForbiddenException("Test Exception")));
    }

    @Test
    public void handleInternalException() {
        responseExceptionHandler = new ResponseExceptionHandler();

        assertNotNull(
            responseExceptionHandler.handleInternalException(
                new InternalException("Test Exception")));
    }



}


