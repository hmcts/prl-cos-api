package uk.gov.hmcts.reform.prl.services.managedocuments;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConcurrencyException extends RuntimeException {

    public ConcurrencyException(String message) {
        super(message);
    }

    public ConcurrencyException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConcurrencyException(String message, UUID documentId, FeignException.Conflict cEx) {
        super((message + documentId), cEx);
    }
}
