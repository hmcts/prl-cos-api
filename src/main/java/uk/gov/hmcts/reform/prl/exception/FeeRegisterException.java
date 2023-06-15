package uk.gov.hmcts.reform.prl.exception;

import lombok.Getter;

@Getter
public class FeeRegisterException extends RuntimeException {

    public FeeRegisterException(String message) {
        super(message);
    }
}
