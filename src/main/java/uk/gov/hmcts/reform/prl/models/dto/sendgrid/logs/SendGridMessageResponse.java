package uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendGridMessageResponse {
    @JsonProperty("from_email")
    private String fromEmail;

    @JsonProperty("sg_message_id")
    private String sgMessageId;

    private String subject;

    @JsonProperty("to_email")
    private String toEmail;

    private String status;

    @JsonProperty("template_id")
    private String templateId;

    @JsonProperty("custom_args")
    private CustomArgs customArgs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomArgs {
        private String caseReference;

        @JsonProperty("sg_template_id")
        private String sgTemplateId;

        @JsonProperty("sg_template_name")
        private String sgTemplateName;
    }
}
