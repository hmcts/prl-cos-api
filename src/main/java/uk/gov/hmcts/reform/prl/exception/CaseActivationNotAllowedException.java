package uk.gov.hmcts.reform.prl.exception;

public class CaseActivationNotAllowedException extends RuntimeException {
    public CaseActivationNotAllowedException() {
        super("The case is not ready for citizen activation");
    }
}
