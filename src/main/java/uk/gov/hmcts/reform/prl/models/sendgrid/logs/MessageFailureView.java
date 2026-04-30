package uk.gov.hmcts.reform.prl.models.sendgrid.logs;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class MessageFailureView {
    private final String caseReference;
    private final String courtName;
    private final OffsetDateTime sentDate;
    private final String subject;
    private final String status;
    private final String reason;
    private final String toEmailAddress;
    private final String messageId;
    private final String templateId;
    private final String templateName;
}
