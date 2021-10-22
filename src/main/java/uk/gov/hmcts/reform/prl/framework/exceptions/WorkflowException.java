package uk.gov.hmcts.reform.prl.framework.exceptions;

public class WorkflowException extends Exception {

    public WorkflowException(String message, Throwable cause) {
        super(message, cause);
    }
}
