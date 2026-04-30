package uk.gov.hmcts.reform.prl.services.sendgrid.logs.failure.report;

import uk.gov.hmcts.reform.prl.models.sendgrid.logs.MessageFailureView;

import java.util.List;

public interface MessageFailureHandler {

    void handle(List<MessageFailureView> messageFailures);
}
