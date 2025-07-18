package uk.gov.hmcts.reform.prl.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@ResponseStatus(UNPROCESSABLE_ENTITY)
public class CourtLocationUnprocessableException extends RuntimeException {
    public CourtLocationUnprocessableException(String message) {
        super(message);
    }
}
