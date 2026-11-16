package uk.gov.hmcts.reform.prl.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@ResponseStatus(SERVICE_UNAVAILABLE)
public class OsCourtLookupException extends RuntimeException {
    public OsCourtLookupException(Throwable cause) {
        super("OS postcode lookup failed", cause);
    }
}
