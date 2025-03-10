package uk.gov.hmcts.reform.prl.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class NoCaseDataFoundException extends ResponseStatusException {
    public NoCaseDataFoundException(HttpStatusCode statusCode, String message) {
        super(statusCode, message);
    }
}
