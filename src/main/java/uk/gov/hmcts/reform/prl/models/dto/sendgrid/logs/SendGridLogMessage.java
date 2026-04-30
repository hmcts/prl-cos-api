package uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class SendGridLogMessage {
    private final SendGridLog sendGridLog;
    private final SendGridMessageResponse sendGridMessageResponse;
}
