package uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendGridLog {
    @JsonProperty("from_email")
    private String fromEmail;

    @JsonProperty("sg_message_id")
    private String sgMessageId;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("to_email")
    private String toEmail;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("status")
    private String status;

    @JsonProperty("sg_message_id_created_at")
    private String sgMessageIdCreatedAt;
}

