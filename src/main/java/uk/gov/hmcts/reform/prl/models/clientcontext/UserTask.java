package uk.gov.hmcts.reform.prl.models.clientcontext;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class UserTask {
    @JsonProperty("task_data")
    private Map<String, Object> taskData;

    @JsonProperty("complete_task")
    private boolean completeTask;
}
