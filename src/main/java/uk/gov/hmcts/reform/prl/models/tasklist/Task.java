package uk.gov.hmcts.reform.prl.models.tasklist;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.Event;
//import uk.gov.hmcts.reform.prl.enums.TaskState;

import java.util.Optional;

@Data
@Builder
public class Task {

    private final Event event;
    private final TaskState state;
    private String hint;

    public Task withHint(String hint) {
        this.setHint(hint);
        return this;
    }

    public Optional<String> getHint() {
        return Optional.ofNullable(hint);
    }

    public static Task task(Event event, TaskState state) {
        return Task.builder()
            .event(event)
            .state(state)
            .build();
    }
}
