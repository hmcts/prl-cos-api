package uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendGridLogsRequest {
    private String query;
    private int limit;
}
