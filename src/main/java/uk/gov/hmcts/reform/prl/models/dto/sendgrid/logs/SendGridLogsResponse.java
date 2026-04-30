package uk.gov.hmcts.reform.prl.models.dto.sendgrid.logs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendGridLogsResponse {
    @JsonProperty("messages")
    private List<SendGridLog> messages;
}
