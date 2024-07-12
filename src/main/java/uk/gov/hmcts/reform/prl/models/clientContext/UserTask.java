package uk.gov.hmcts.reform.prl.models.clientContext;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class UserTask {
    @JsonProperty("task_data")
    private Map<String, Object> task_data;

    @JsonProperty("complete_task")
    private boolean complete_task;
}
