package uk.gov.hmcts.reform.prl.models.clientcontext;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientContext {
    @JsonProperty("user_task")
    private UserTask userTask;
}
