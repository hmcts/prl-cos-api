package uk.gov.hmcts.reform.prl.models.tasklist;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;

import java.util.Optional;

@Data
@Builder
public class RespondentTask {

    private final RespondentSolicitorEvents event;
    private final TaskState state;
    private String hint;

    public RespondentTask withHint(String hint) {
        this.setHint(hint);
        return this;
    }

    public Optional<String> getHint() {
        return Optional.ofNullable(hint);
    }

    public static RespondentTask task(RespondentSolicitorEvents event, TaskState state) {
        return RespondentTask.builder()
            .event(event)
            .state(state)
            .build();
    }
}
